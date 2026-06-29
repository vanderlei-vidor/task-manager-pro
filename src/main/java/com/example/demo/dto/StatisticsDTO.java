package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {

    private Long total;
    private Long concluidas;
    private Long pendentes;
    private Long emAndamento;
    private Long atrasadas;
    private Long altaPrioridade;
    private Long mediaPrioridade;
    private Long baixaPrioridade;
    private Double taxaConclusao;
    private String melhorDia;
    private Double produtividade;
}