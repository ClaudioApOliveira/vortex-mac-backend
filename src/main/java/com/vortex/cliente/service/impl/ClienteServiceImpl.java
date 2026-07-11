package com.vortex.cliente.service.impl;

import com.vortex.cliente.dto.ClienteRequest;
import com.vortex.cliente.dto.ClienteResponse;
import com.vortex.cliente.entity.Cliente;
import com.vortex.cliente.entity.TipoPessoa;
import com.vortex.cliente.repository.ClienteRepository;
import com.vortex.cliente.service.ClienteService;
import com.vortex.endereco.dto.EnderecoRequest;
import com.vortex.endereco.entity.Endereco;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.NotFoundException;
import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;

  @Inject
  public ClienteServiceImpl(
      ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
    this.clienteRepository = clienteRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  @Transactional
  public List<ClienteResponse> listarTodos() {
    return clienteRepository.findAll().stream().map(ClienteResponse::from).toList();
  }

  @Override
  @Transactional
  public ClienteResponse buscarPorId(Long id) {
    Cliente cliente = buscarEntidadePorId(id);
    return ClienteResponse.from(cliente);
  }

  @Override
  @Transactional
  public ClienteResponse criar(ClienteRequest request) {
    validarDadosPorTipoPessoa(request);
    validarDocumentoUnico(request, null);
    validarEmailUnico(request.email(), null);

    Cliente cliente = new Cliente();
    aplicarDados(cliente, request);

    clienteRepository.save(cliente);
    criarUsuarioCliente(cliente, request.email());

    return ClienteResponse.from(cliente);
  }

  @Override
  @Transactional
  public ClienteResponse atualizar(Long id, ClienteRequest request) {
    Cliente cliente = buscarEntidadePorId(id);
    validarDadosPorTipoPessoa(request);
    validarDocumentoUnico(request, id);

    Usuario usuario = cliente.getUsuario();
    if (usuario == null) {
      throw new BusinessException("Cliente sem usuário vinculado");
    }

    validarEmailUnico(request.email(), usuario.getId());
    aplicarDados(cliente, request);
    atualizarUsuarioCliente(usuario, cliente, request.email());

    return ClienteResponse.from(cliente);
  }

  @Override
  @Transactional
  public void excluir(Long id) {
    Cliente cliente = buscarEntidadePorId(id);

    if (cliente.getUsuario() != null) {
      usuarioRepository.delete(cliente.getUsuario());
    }

    clienteRepository.delete(cliente);
  }

  public Cliente buscarEntidadePorId(Long id) {
    return clienteRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Cliente não encontrado com id: " + id));
  }

  private void criarUsuarioCliente(Cliente cliente, String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email.trim().toLowerCase());
    usuario.setNome(cliente.getNome());
    usuario.setPerfil(Perfil.CLIENTE);
    usuario.setSenha(null);
    usuario.setDeveDefinirSenha(true);
    usuario.setCliente(cliente);
    usuarioRepository.save(usuario);
    cliente.setUsuario(usuario);
  }

  private void atualizarUsuarioCliente(Usuario usuario, Cliente cliente, String email) {
    usuario.setEmail(email.trim().toLowerCase());
    usuario.setNome(cliente.getNome());
  }

  private void validarEmailUnico(String email, Long usuarioId) {
    boolean emailEmUso =
        usuarioId == null
            ? usuarioRepository.existsByEmail(email.trim().toLowerCase())
            : usuarioRepository.existsByEmailAndIdNot(email.trim().toLowerCase(), usuarioId);

    if (emailEmUso) {
      throw new BusinessException("Email já cadastrado: " + email);
    }
  }

  private void validarDadosPorTipoPessoa(ClienteRequest request) {
    if (request.tipoPessoa() == TipoPessoa.PESSOA_FISICA) {
      if (request.nome() == null || request.nome().isBlank()) {
        throw new BusinessException("Nome é obrigatório para pessoa física");
      }
      if (request.cpf() == null || request.cpf().isBlank()) {
        throw new BusinessException("CPF é obrigatório para pessoa física");
      }
      return;
    }

    if (request.razaoSocial() == null || request.razaoSocial().isBlank()) {
      throw new BusinessException("Razão social é obrigatória para pessoa jurídica");
    }
    if (request.cnpj() == null || request.cnpj().isBlank()) {
      throw new BusinessException("CNPJ é obrigatório para pessoa jurídica");
    }
  }

  private void validarDocumentoUnico(ClienteRequest request, Long id) {
    if (request.tipoPessoa() == TipoPessoa.PESSOA_FISICA) {
      validarCpfUnico(request.cpf(), id);
      return;
    }

    validarCnpjUnico(request.cnpj(), id);
  }

  private void validarCpfUnico(String cpf, Long id) {
    if (cpf == null || cpf.isBlank()) {
      return;
    }

    boolean cpfEmUso =
        id == null
            ? clienteRepository.existsByCpf(cpf)
            : clienteRepository.existsByCpfAndIdNot(cpf, id);

    if (cpfEmUso) {
      throw new BusinessException("CPF já cadastrado: " + cpf);
    }
  }

  private void validarCnpjUnico(String cnpj, Long id) {
    if (cnpj == null || cnpj.isBlank()) {
      return;
    }

    boolean cnpjEmUso =
        id == null
            ? clienteRepository.existsByCnpj(cnpj)
            : clienteRepository.existsByCnpjAndIdNot(cnpj, id);

    if (cnpjEmUso) {
      throw new BusinessException("CNPJ já cadastrado: " + cnpj);
    }
  }

  private void aplicarDados(Cliente cliente, ClienteRequest request) {
    cliente.setTipoPessoa(request.tipoPessoa());
    cliente.setTelefone(request.telefone());
    aplicarEndereco(cliente, request.endereco());

    if (request.tipoPessoa() == TipoPessoa.PESSOA_FISICA) {
      cliente.setNome(request.nome());
      cliente.setCpf(normalizarCpf(request.cpf()));
      cliente.setCnpj(null);
      cliente.setRazaoSocial(null);
      cliente.setNomeFantasia(null);
      return;
    }

    cliente.setRazaoSocial(request.razaoSocial());
    cliente.setNomeFantasia(request.nomeFantasia());
    cliente.setCnpj(normalizarCnpj(request.cnpj()));
    cliente.setNome(
        request.nomeFantasia() != null && !request.nomeFantasia().isBlank()
            ? request.nomeFantasia()
            : request.razaoSocial());
    cliente.setCpf(null);
  }

  private void aplicarEndereco(Cliente cliente, EnderecoRequest request) {
    if (request == null) {
      cliente.setEndereco(null);
      return;
    }

    Endereco endereco = cliente.getEndereco();
    if (endereco == null) {
      endereco = new Endereco();
      cliente.setEndereco(endereco);
    }

    endereco.setCep(normalizarCep(request.cep()));
    endereco.setLogradouro(request.logradouro());
    endereco.setComplemento(request.complemento());
    endereco.setNumero(request.numero());
    endereco.setBairro(request.bairro());
    endereco.setCidade(request.cidade());
    endereco.setUf(request.uf());
    endereco.setEstado(request.estado());
    endereco.setIbge(request.ibge());
  }

  private String normalizarCep(String cep) {
    if (cep == null) {
      return null;
    }
    String apenasDigitos = cep.replaceAll("\\D", "");
    if (apenasDigitos.length() != 8) {
      return cep;
    }
    return apenasDigitos.substring(0, 5) + "-" + apenasDigitos.substring(5);
  }

  private String normalizarCpf(String cpf) {
    if (cpf == null) {
      return null;
    }
    String apenasDigitos = cpf.replaceAll("\\D", "");
    if (apenasDigitos.length() != 11) {
      return cpf;
    }
    return apenasDigitos.substring(0, 3)
        + "."
        + apenasDigitos.substring(3, 6)
        + "."
        + apenasDigitos.substring(6, 9)
        + "-"
        + apenasDigitos.substring(9);
  }

  private String normalizarCnpj(String cnpj) {
    if (cnpj == null) {
      return null;
    }
    String apenasDigitos = cnpj.replaceAll("\\D", "");
    if (apenasDigitos.length() != 14) {
      return cnpj;
    }
    return apenasDigitos.substring(0, 2)
        + "."
        + apenasDigitos.substring(2, 5)
        + "."
        + apenasDigitos.substring(5, 8)
        + "/"
        + apenasDigitos.substring(8, 12)
        + "-"
        + apenasDigitos.substring(12);
  }
}
