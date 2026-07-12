package com.vortex.auth.service;

import com.vortex.auth.dto.AlterarSenhaRequest;
import com.vortex.auth.dto.AtualizarPerfilRequest;
import com.vortex.auth.dto.LoginRequest;
import com.vortex.auth.dto.PrimeiroAcessoRequest;
import com.vortex.auth.dto.TokenResponse;
import com.vortex.auth.dto.UsuarioAutenticadoResponse;
import com.vortex.auth.dto.VerificarPrimeiroAcessoRequest;
import com.vortex.auth.dto.VerificarPrimeiroAcessoResponse;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.ordemservico.dto.OrdemServicoStatusHistoricoResponse;
import com.vortex.shared.response.PageResponse;
import java.util.List;

public interface AuthService {

  TokenResponse autenticar(LoginRequest request);

  VerificarPrimeiroAcessoResponse verificarPrimeiroAcesso(VerificarPrimeiroAcessoRequest request);

  TokenResponse definirSenhaPrimeiroAcesso(PrimeiroAcessoRequest request);

  TokenResponse renovarToken(String refreshToken);

  void logout(String refreshToken);

  UsuarioAutenticadoResponse obterUsuarioAutenticado();

  UsuarioAutenticadoResponse atualizarPerfil(AtualizarPerfilRequest request);

  TokenResponse alterarSenha(AlterarSenhaRequest request);

  List<OrdemServicoResponse> listarMinhasOrdensServico();

  PageResponse<OrdemServicoResponse> listarMinhasOrdensServicoPaginado(int page, int size);

  OrdemServicoResponse buscarMinhaOrdemServico(Long id);

  OrdemServicoResponse aprovarMinhaOrdemServico(Long id);

  OrdemServicoResponse rejeitarMinhaOrdemServico(Long id);

  List<OrdemServicoStatusHistoricoResponse> listarHistoricoMinhaOrdemServico(Long id);
}
