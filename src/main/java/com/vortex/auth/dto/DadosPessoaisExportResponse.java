package com.vortex.auth.dto;

import com.vortex.cliente.dto.ClienteResponse;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.veiculo.dto.VeiculoResponse;
import java.time.LocalDateTime;
import java.util.List;

public record DadosPessoaisExportResponse(
    LocalDateTime exportadoEm,
    String versaoPolitica,
    UsuarioAutenticadoResponse usuario,
    ClienteResponse cliente,
    List<VeiculoResponse> veiculos,
    List<OrdemServicoResponse> ordensServico) {}
