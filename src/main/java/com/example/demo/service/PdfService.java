package com.example.demo.service;

import com.example.demo.model.Task;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.Color; // 💡 Para as cores do cabeçalho
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public void gerarPdfTarefas(HttpServletResponse response, List<Task> tarefas) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36); // 💡 Adicionado margens limpas
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        
        // Fontes estilizadas
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font cabecalhoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        Font corpoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

        // Título do Relatório
        Paragraph titulo = new Paragraph("Relatório de Tarefas - Gestor Pro", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" ")); // Espaço em branco

        // Criamos a tabela com 5 colunas
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2.0f, 3.5f, 1.5f, 1.5f, 1.5f });

        // 1. Configurando Cabeçalhos Estilizados
        String[] colunas = { "Título", "Descrição", "Status", "Prazo", "Criada em" };
        for (String coluna : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(coluna, cabecalhoFont));
            cell.setBackgroundColor(new Color(67, 97, 238)); // 💡 Azul padrão do Gestor Pro
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 2. Preenchendo os Dados com Segurança
        for (Task task : tarefas) {
            // Título (Tratando possível nulo)
            table.addCell(new PdfPCell(new Phrase(task.getTitle() != null ? task.getTitle() : "-", corpoFont)));
            
            // Descrição
            table.addCell(new PdfPCell(new Phrase(task.getDescription() != null ? task.getDescription() : "-", corpoFont)));
            
            // Status (Alinhado ao centro)
            PdfPCell cellStatus = new PdfPCell(new Phrase(task.getStatus() != null ? task.getStatus().toString() : "-", corpoFont));
            cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellStatus);

            // Formata o Prazo
            String prazo = task.getDueDate() != null ? task.getDueDate().format(formatterData) : "-";
            PdfPCell cellPrazo = new PdfPCell(new Phrase(prazo, corpoFont));
            cellPrazo.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellPrazo);

            // Formata a Data de Criação
            String criacao = task.getDataCriacao() != null ? task.getDataCriacao().format(formatterHora) : "-";
            PdfPCell cellCriacao = new PdfPCell(new Phrase(criacao, corpoFont));
            cellCriacao.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellCriacao);
        }

        document.add(table);
        document.close();
    }
}