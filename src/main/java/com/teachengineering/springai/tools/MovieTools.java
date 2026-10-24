package com.teachengineering.springai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;

public class MovieTools {

    private final Map<String, Double> notas = Map.of(
            "Goodfellas", 8.7,
            "Pulp Fiction", 8.9,
            "Heat", 8.3,
            "Blade Runner", 8.1);

    @Tool
    public String buscarNotaFilme(@ToolParam(description = "Titulo original do filme, em inglês") String titulo) {
        Double nota = notas.get(titulo);
        if (nota == null) {
            return "Filme não encontrado na base";
        }
        return String.format("A nota do filme %s é %.1f", titulo, nota);
    }
}
