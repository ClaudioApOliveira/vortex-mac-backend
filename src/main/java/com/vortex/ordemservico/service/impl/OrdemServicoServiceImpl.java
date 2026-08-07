package com.vortex.ordemservico.service.impl;

import com.vortex.auth.service.UsuarioAutenticadoProvider;
import com.vortex.cliente.entity.Cliente;
import com.vortex.cliente.repository.ClienteRepository;
import com.vortex.ordemservico.dto.OrdemServicoItemRequest;
import com.vortex.ordemservico.dto.OrdemServicoFiltro;
import com.vortex.ordemservico.dto.OrdemServicoRequest;
import com.vortex.ordemservico.dto.OrdemServicoStatusRequest;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.ordemservico.dto.OrdemServicoStatusHistoricoResponse;
import com.vortex.ordemservico.entity.OrdemServico;
import com.vortex.ordemservico.entity.OrdemServicoItem;
import com.vortex.ordemservico.entity.OrdemServicoItemTipo;
import com.vortex.ordemservico.entity.OrdemServicoStatus;
import com.vortex.ordemservico.entity.OrdemServicoStatusHistorico;
import com.vortex.ordemservico.entity.OrdemServicoStatusHistoricoOrigem;
import com.vortex.ordemservico.repository.OrdemServicoRepository;
import com.vortex.ordemservico.repository.OrdemServicoStatusHistoricoRepository;
import com.vortex.ordemservico.service.OrdemServicoService;
import com.vortex.ordemservico.service.OrdemServicoStatusTransitions;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.NotFoundException;
import com.vortex.shared.response.PageResponse;
import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import com.vortex.veiculo.entity.Veiculo;
import com.vortex.veiculo.repository.VeiculoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@ApplicationScoped
public class OrdemServicoServiceImpl implements OrdemServicoService {

  private final OrdemServicoRepository ordemServicoRepository;
  private final OrdemServicoStatusHistoricoRepository statusHistoricoRepository;
  private final ClienteRepository clienteRepository;
  private final VeiculoRepository veiculoRepository;
  private final UsuarioRepository usuarioRepository;
  private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

  @Inject
  public OrdemServicoServiceImpl(
      OrdemServicoRepository ordemServicoRepository,
      OrdemServicoStatusHistoricoRepository statusHistoricoRepository,
      ClienteRepository clienteRepository,
      VeiculoRepository veiculoRepository,
      UsuarioRepository usuarioRepository,
      UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
    this.ordemServicoRepository = ordemServicoRepository;
    this.statusHistoricoRepository = statusHistoricoRepository;
    this.clienteRepository = clienteRepository;
    this.veiculoRepository = veiculoRepository;
    this.usuarioRepository = usuarioRepository;
    this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
  }

  @Override
  @Transactional
  public List<OrdemServicoResponse> listarTodos() {
    return ordemServicoRepository.findAll().stream().map(OrdemServicoResponse::from).toList();
  }

  @Override
  @Transactional
  public PageResponse<OrdemServicoResponse> listarPaginado(
      int page, int size, OrdemServicoFiltro filtro) {
    int pagina = normalizarPagina(page);
    int tamanho = normalizarTamanho(size);
    OrdemServicoFiltro filtroEfetivo =
        filtro != null
            ? filtro
            : new OrdemServicoFiltro(null, null, null, null, null);

    long total =
        filtroEfetivo.temFiltros()
            ? ordemServicoRepository.countFiltered(filtroEfetivo)
            : ordemServicoRepository.countAll();
    List<OrdemServicoResponse> content =
        (filtroEfetivo.temFiltros()
                ? ordemServicoRepository.findFiltered(filtroEfetivo, pagina, tamanho)
                : ordemServicoRepository.findAllPaginated(pagina, tamanho))
            .stream().map(OrdemServicoResponse::from).toList();

    return PageResponse.of(content, pagina, tamanho, total);
  }

  @Override
  @Transactional
  public List<OrdemServicoResponse> listarPorCliente(Long clienteId) {
    validarClienteExistente(clienteId);
    return ordemServicoRepository.findByClienteId(clienteId).stream()
        .map(OrdemServicoResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public List<OrdemServicoResponse> listarPorVeiculo(Long veiculoId) {
    buscarVeiculoPorId(veiculoId);
    return ordemServicoRepository.findByVeiculoId(veiculoId).stream()
        .map(OrdemServicoResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public OrdemServicoResponse buscarPorId(Long id) {
    return OrdemServicoResponse.from(buscarEntidadePorId(id));
  }

  @Override
  @Transactional
  public List<OrdemServicoResponse> listarPorUsuarioCliente(Long usuarioId) {
    Long clienteId = obterClienteIdVinculado(usuarioId);
    return listarPorCliente(clienteId);
  }

  @Override
  @Transactional
  public PageResponse<OrdemServicoResponse> listarPaginadoPorUsuarioCliente(
      Long usuarioId, int page, int size) {
    Long clienteId = obterClienteIdVinculado(usuarioId);
    int pagina = normalizarPagina(page);
    int tamanho = normalizarTamanho(size);

    long total = ordemServicoRepository.countByClienteId(clienteId);
    List<OrdemServicoResponse> content =
        ordemServicoRepository.findByClienteIdPaginated(clienteId, pagina, tamanho).stream()
            .map(OrdemServicoResponse::from)
            .toList();

    return PageResponse.of(content, pagina, tamanho, total);
  }

  @Override
  @Transactional
  public OrdemServicoResponse buscarPorUsuarioCliente(Long usuarioId, Long ordemId) {
    return OrdemServicoResponse.from(obterOrdemDoCliente(usuarioId, ordemId));
  }

  @Override
  @Transactional
  public OrdemServicoResponse aprovarPorUsuarioCliente(Long usuarioId, Long ordemId) {
    OrdemClienteContexto contexto = obterOrdemDoClienteComUsuario(usuarioId, ordemId);
    OrdemServico ordemServico = contexto.ordemServico();
    validarOrcamentoPendente(ordemServico);

    OrdemServicoStatus statusAnterior = ordemServico.getStatus();
    ordemServico.setStatus(OrdemServicoStatus.APROVADO);

    registrarHistoricoStatus(
        ordemServico,
        statusAnterior,
        OrdemServicoStatus.APROVADO,
        contexto.usuario(),
        OrdemServicoStatusHistoricoOrigem.CLIENTE,
        "Orçamento aprovado pelo cliente");

    return OrdemServicoResponse.from(ordemServico);
  }

  @Override
  @Transactional
  public OrdemServicoResponse rejeitarPorUsuarioCliente(Long usuarioId, Long ordemId) {
    OrdemClienteContexto contexto = obterOrdemDoClienteComUsuario(usuarioId, ordemId);
    OrdemServico ordemServico = contexto.ordemServico();
    validarOrcamentoPendente(ordemServico);

    OrdemServicoStatus statusAnterior = ordemServico.getStatus();
    ordemServico.setStatus(OrdemServicoStatus.CANCELADO);

    registrarHistoricoStatus(
        ordemServico,
        statusAnterior,
        OrdemServicoStatus.CANCELADO,
        contexto.usuario(),
        OrdemServicoStatusHistoricoOrigem.CLIENTE,
        "Orçamento rejeitado pelo cliente");

    return OrdemServicoResponse.from(ordemServico);
  }

  @Override
  @Transactional
  public List<OrdemServicoStatusHistoricoResponse> listarHistoricoStatus(Long ordemId) {
    buscarEntidadePorId(ordemId);
    return statusHistoricoRepository.findByOrdemServicoIdOrderByCriadoEmDesc(ordemId).stream()
        .map(OrdemServicoStatusHistoricoResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public List<OrdemServicoStatusHistoricoResponse> listarHistoricoStatusPorUsuarioCliente(
      Long usuarioId, Long ordemId) {
    obterOrdemDoCliente(usuarioId, ordemId);
    return statusHistoricoRepository.findByOrdemServicoIdOrderByCriadoEmDesc(ordemId).stream()
        .map(OrdemServicoStatusHistoricoResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public OrdemServicoResponse criar(OrdemServicoRequest request) {
    OrdemServico ordemServico = new OrdemServico();
    ordemServico.setStatus(OrdemServicoStatus.ORCAMENTO);
    aplicarDados(ordemServico, request, false);
    ordemServicoRepository.save(ordemServico);

    Usuario usuario = usuarioAutenticadoProvider.obterUsuarioAutenticado().orElse(null);
    registrarHistoricoStatus(
        ordemServico,
        null,
        OrdemServicoStatus.ORCAMENTO,
        usuario,
        mapearOrigem(usuario),
        "Ordem de serviço registrada como orçamento");

    return OrdemServicoResponse.from(ordemServico);
  }

  @Override
  @Transactional
  public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
    OrdemServico ordemServico = buscarEntidadePorId(id);
    validarOrdemEditavel(ordemServico);
    OrdemServicoStatus statusAnterior = ordemServico.getStatus();
    Usuario usuario = usuarioAutenticadoProvider.obterUsuarioAutenticadoObrigatorio();

    validarAlteracaoStatus(usuario, statusAnterior, request.status());

    ordemServico.getItens().clear();
    aplicarDados(ordemServico, request, true);

    if (request.status() != null && statusAnterior != ordemServico.getStatus()) {
      registrarHistoricoStatus(
          ordemServico,
          statusAnterior,
          ordemServico.getStatus(),
          usuario,
          mapearOrigem(usuario),
          null);
    }

    return OrdemServicoResponse.from(ordemServico);
  }

  @Override
  @Transactional
  public OrdemServicoResponse alterarStatus(Long id, OrdemServicoStatusRequest request) {
    OrdemServico ordemServico = buscarEntidadePorId(id);
    OrdemServicoStatus statusAnterior = ordemServico.getStatus();
    Usuario usuario = usuarioAutenticadoProvider.obterUsuarioAutenticadoObrigatorio();

    validarAlteracaoStatus(usuario, statusAnterior, request.status());

    ordemServico.setStatus(request.status());
    String observacao = request.observacao() != null ? request.observacao().trim() : null;
    if (observacao != null && observacao.isBlank()) {
      observacao = null;
    }
    if (observacao == null && request.status() == OrdemServicoStatus.ORCAMENTO) {
      observacao = "Orçamento reaberto para revisão";
    }

    registrarHistoricoStatus(
        ordemServico,
        statusAnterior,
        ordemServico.getStatus(),
        usuario,
        mapearOrigem(usuario),
        observacao);

    return OrdemServicoResponse.from(ordemServico);
  }

  @Override
  @Transactional
  public void excluir(Long id) {
    Usuario usuario = usuarioAutenticadoProvider.obterUsuarioAutenticadoObrigatorio();
    if (usuario.getPerfil() != Perfil.ADMIN) {
      throw new ForbiddenException("Somente administradores podem excluir ordens de serviço.");
    }

    OrdemServico ordemServico = buscarEntidadePorId(id);
    if (ordemServico.getStatus() == OrdemServicoStatus.CONCLUIDO
        || ordemServico.getStatus() == OrdemServicoStatus.CANCELADO) {
      throw new BusinessException(
          "Ordens concluídas ou canceladas não podem ser excluídas.");
    }
    ordemServicoRepository.delete(ordemServico);
  }

  private OrdemServico buscarEntidadePorId(Long id) {
    return ordemServicoRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada com id: " + id));
  }

  private Cliente validarClienteExistente(Long clienteId) {
    return clienteRepository
        .findById(clienteId)
        .orElseThrow(() -> new NotFoundException("Cliente não encontrado com id: " + clienteId));
  }

  private OrdemServico obterOrdemDoCliente(Long usuarioId, Long ordemId) {
    return obterOrdemDoClienteComUsuario(usuarioId, ordemId).ordemServico();
  }

  private OrdemClienteContexto obterOrdemDoClienteComUsuario(Long usuarioId, Long ordemId) {
    Usuario usuario = buscarUsuarioCliente(usuarioId);
    Long clienteId = usuario.getCliente().getId();
    OrdemServico ordemServico = buscarEntidadePorId(ordemId);

    if (!ordemServico.getCliente().getId().equals(clienteId)) {
      throw new NotFoundException("Ordem de serviço não encontrada com id: " + ordemId);
    }

    return new OrdemClienteContexto(usuario, ordemServico);
  }

  private record OrdemClienteContexto(Usuario usuario, OrdemServico ordemServico) {}

  private void validarOrcamentoPendente(OrdemServico ordemServico) {
    if (ordemServico.getStatus() != OrdemServicoStatus.ORCAMENTO) {
      throw new BusinessException(
          "Somente orçamentos pendentes podem ser aprovados ou rejeitados.");
    }
  }

  private void validarOrdemEditavel(OrdemServico ordemServico) {
    if (ordemServico.getStatus() == OrdemServicoStatus.CONCLUIDO) {
      throw new BusinessException(
          "Ordem de serviço concluída não pode mais ser alterada.");
    }
    if (ordemServico.getStatus() == OrdemServicoStatus.CANCELADO) {
      throw new BusinessException(
          "Ordem de serviço cancelada não pode ser editada. Reabra o orçamento para revisar.");
    }
  }

  private void validarAlteracaoStatus(
      Usuario usuario, OrdemServicoStatus statusAtual, OrdemServicoStatus statusSolicitado) {
    if (statusSolicitado == null || statusSolicitado == statusAtual) {
      return;
    }

    if (!OrdemServicoStatusTransitions.podeTransicionar(statusAtual, statusSolicitado)) {
      throw new BusinessException(
          "Transição de status não permitida: "
              + statusAtual
              + " → "
              + statusSolicitado
              + ".");
    }

    if (!OrdemServicoStatusTransitions.podeDefinirStatus(usuario.getPerfil(), statusSolicitado)) {
      throw new BusinessException(
          "Somente o cliente, gerente ou administrador pode aprovar orçamentos.");
    }
  }

  private Long obterClienteIdVinculado(Long usuarioId) {
    return buscarUsuarioCliente(usuarioId).getCliente().getId();
  }

  private Usuario buscarUsuarioCliente(Long usuarioId) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(
                () -> new NotFoundException("Usuário não encontrado com id: " + usuarioId));

    if (usuario.getPerfil() != Perfil.CLIENTE || usuario.getCliente() == null) {
      throw new BusinessException(
          "Histórico de ordens de serviço disponível apenas para usuários com perfil CLIENTE.");
    }

    return usuario;
  }

  private Veiculo buscarVeiculoPorId(Long veiculoId) {
    return veiculoRepository
        .findById(veiculoId)
        .orElseThrow(() -> new NotFoundException("Veículo não encontrado com id: " + veiculoId));
  }

  private Usuario validarTecnico(Long tecnicoId) {
    Usuario tecnico =
        usuarioRepository
            .findById(tecnicoId)
            .orElseThrow(
                () -> new NotFoundException("Responsável não encontrado com id: " + tecnicoId));

    if (!tecnico.isAtivo()) {
      throw new BusinessException("Responsável inativo: " + tecnico.getNome());
    }

    if (tecnico.getPerfil() == Perfil.CLIENTE) {
      throw new BusinessException("Usuário informado não pode ser responsável pela OS: " + tecnico.getNome());
    }

    return tecnico;
  }

  private void aplicarDados(
      OrdemServico ordemServico, OrdemServicoRequest request, boolean permitirStatus) {
    Cliente cliente = validarClienteExistente(request.clienteId());
    Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());

    if (!veiculo.getCliente().getId().equals(cliente.getId())) {
      throw new BusinessException("Veículo não pertence ao proprietário informado");
    }

    validarQuilometragem(request.kmEntrada(), request.kmSaida());

    ordemServico.setCliente(cliente);
    ordemServico.setVeiculo(veiculo);
    ordemServico.setTecnico(validarTecnico(request.tecnicoId()));
    ordemServico.setData(request.data());
    ordemServico.setHora(request.hora());
    ordemServico.setKmEntrada(request.kmEntrada());
    ordemServico.setKmSaida(request.kmSaida());
    ordemServico.setCustoServicosTerceirizados(
        normalizarValor(request.custoServicosTerceirizados()));
    ordemServico.setDescricaoServicosTerceirizados(
        normalizarTexto(request.descricaoServicosTerceirizados()));
    ordemServico.setCustoMaoDeObra(normalizarValor(request.custoMaoDeObra()));
    ordemServico.setDescricaoMaoDeObra(normalizarTexto(request.descricaoMaoDeObra()));
    ordemServico.setDiagnosticoInicial(normalizarTexto(request.diagnosticoInicial()));

    if (permitirStatus && request.status() != null) {
      ordemServico.setStatus(request.status());
    }

    for (OrdemServicoItemRequest itemRequest : request.itens()) {
      if (itemRequest.tipo() != null && itemRequest.tipo() != OrdemServicoItemTipo.PECA) {
        throw new BusinessException("Itens da ordem de serviço devem ser do tipo peça");
      }
      OrdemServicoItem item = new OrdemServicoItem();
      item.setOrdemServico(ordemServico);
      item.setDescricao(itemRequest.descricao().trim());
      item.setQuantidade(normalizarValor(itemRequest.quantidade()));
      item.setValorUnitario(normalizarValor(itemRequest.valorUnitario()));
      item.setTipo(OrdemServicoItemTipo.PECA);
      ordemServico.getItens().add(item);
    }

    aplicarTotais(ordemServico);
  }

  private void registrarHistoricoStatus(
      OrdemServico ordemServico,
      OrdemServicoStatus statusAnterior,
      OrdemServicoStatus statusNovo,
      Usuario usuario,
      OrdemServicoStatusHistoricoOrigem origem,
      String observacao) {
    OrdemServicoStatusHistorico historico = new OrdemServicoStatusHistorico();
    historico.setOrdemServico(ordemServico);
    historico.setStatusAnterior(statusAnterior);
    historico.setStatusNovo(statusNovo);
    historico.setUsuario(usuario);
    historico.setOrigem(origem != null ? origem : OrdemServicoStatusHistoricoOrigem.SISTEMA);
    historico.setObservacao(observacao);
    statusHistoricoRepository.save(historico);
  }

  private OrdemServicoStatusHistoricoOrigem mapearOrigem(Usuario usuario) {
    if (usuario == null) {
      return OrdemServicoStatusHistoricoOrigem.SISTEMA;
    }

    return switch (usuario.getPerfil()) {
      case ADMIN -> OrdemServicoStatusHistoricoOrigem.ADMIN;
      case GERENTE -> OrdemServicoStatusHistoricoOrigem.GERENTE;
      case TECNICO -> OrdemServicoStatusHistoricoOrigem.TECNICO;
      case CLIENTE -> OrdemServicoStatusHistoricoOrigem.CLIENTE;
    };
  }

  private void validarQuilometragem(Integer kmEntrada, Integer kmSaida) {
    if (kmEntrada != null && kmSaida != null && kmSaida < kmEntrada) {
      throw new BusinessException("KM de saída não pode ser menor que KM de entrada");
    }
  }

  private void aplicarTotais(OrdemServico ordemServico) {
    BigDecimal custoPecas = BigDecimal.ZERO;

    for (OrdemServicoItem item : ordemServico.getItens()) {
      custoPecas = custoPecas.add(item.getQuantidade().multiply(item.getValorUnitario()));
    }

    ordemServico.setCustoPecas(normalizarValor(custoPecas));
    ordemServico.setPrecoTotal(
        normalizarValor(
            ordemServico
                .getCustoServicosTerceirizados()
                .add(ordemServico.getCustoPecas())
                .add(ordemServico.getCustoMaoDeObra())));
  }

  private BigDecimal normalizarValor(BigDecimal valor) {
    return valor.setScale(2, RoundingMode.HALF_UP);
  }

  private String normalizarTexto(String valor) {
    if (valor == null) {
      return null;
    }
    String texto = valor.trim();
    return texto.isEmpty() ? null : texto;
  }

  private int normalizarPagina(int page) {
    return Math.max(page, 0);
  }

  private int normalizarTamanho(int size) {
    if (size < 1) {
      throw new BusinessException("Tamanho da página deve ser maior que zero.");
    }
    return Math.min(size, 50);
  }
}
