package com.welltech.util;

import com.welltech.model.Consultation; // We'll make it general, but Consultation is the primary target
import org.apache.poi.ss.usermodel.*; // Core styles, Workbook, Sheet, Row, Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For .xlsx format

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    /**
     * Exports a list of Consultations to an Excel (.xlsx) file.
     *
     * @param consultations List of consultations to export.
     * @param filePath      The full path including filename where the Excel file will be saved.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void exportConsultationsToExcel(List<Consultation> consultations, String filePath) throws IOException {
        // Create a new workbook (for .xlsx format)
        Workbook workbook = new XSSFWorkbook();

        // Create a sheet named "Consultations"
        Sheet sheet = workbook.createSheet("Consultations");

        // Create the header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Date & Time", "Patient", "Psychiatrist", "Purpose", "Duration (min)", "Status", "Notes"};
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            // Optional: Apply a bold style to headers
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCell.setCellStyle(headerCellStyle);
        }

        // Populate the data rows
        int rowNum = 1;
        for (Consultation consultation : consultations) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(consultation.getId());
            row.createCell(1).setCellValue(consultation.getConsultationTime() != null ? consultation.getConsultationTime().toString() : "N/A"); // Use a format if needed
            row.createCell(2).setCellValue(consultation.getPatientName());
            row.createCell(3).setCellValue(consultation.getPsychiatristName());
            row.createCell(4).setCellValue(consultation.getPurpose() != null ? consultation.getPurpose() : "");
            row.createCell(5).setCellValue(consultation.getDurationMinutes() > 0 ? consultation.getDurationMinutes() : 0); // Write 0 if N/A
            row.createCell(6).setCellValue(consultation.getStatusDisplayValue());
            row.createCell(7).setCellValue(consultation.getNotes() != null ? consultation.getNotes() : "");
        }

        // Optional: Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            // Close the workbook to free resources
            workbook.close();
        }
    }

    // You could add more generic methods here if needed, e.g., for Users
    /*
    public static <T> void exportDataToExcel(List<T> data, String sheetName, String[] headers, ValueExtractor<T>[] valueExtractors, String filePath) throws IOException {
       // More complex generic export logic
    }

    // Interface to define how to extract values from a generic object
    interface ValueExtractor<T> {
        Object getValue(T item);
    }
    */
}