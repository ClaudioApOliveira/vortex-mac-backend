package com.vortex.endereco.dto;

import com.vortex.endereco.entity.Endereco;

public record EnderecoResponse(
    Long id,
    String cep,
    String logradouro,
    String complemento,
    String numero,
    String bairro,
    String cidade,
    String uf,
    String estado,
    String ibge) {
  public static EnderecoResponse from(Endereco endereco) {
    if (endereco == null) {
      return null;
    }

    return new EnderecoResponse(
        endereco.getId(),
        endereco.getCep(),
        endereco.getLogradouro(),
        endereco.getComplemento(),
        endereco.getNumero(),
        endereco.getBairro(),
        endereco.getCidade(),
        endereco.getUf(),
        endereco.getEstado(),
        endereco.getIbge());
  }
}
