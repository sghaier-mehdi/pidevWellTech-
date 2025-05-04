package com.welltech.util;

import com.welltech.model.Consultation;

// === Apache POI Imports for Styling ===
import org.apache.poi.ss.usermodel.*; // Contains Workbook, Sheet, Row, Cell, CellStyle, Font, IndexedColors, etc.
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // Specific class for .xlsx format
// You might need these for more advanced colors:
// import org.apache.poi.xssf.usermodel.Default******olor;
// import org.apache.poi.xssf.usermodel.XSSFColor;
// import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
// ======================================

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    /**
     * Exports a list of Consultations to an Excel (.xlsx) file with styling.
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

        // --- Create Styles ---

        // Style for Header Row
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex()); // White text for headers

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        // Background color (using IndexedColors for simplicity)
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex()); // Dark grey background
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Alignment
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // Borders (optional, but adds structure)
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());


        // Style for Data Rows (basic style)
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.TOP); // Align text top for better readability in rows
        // Borders
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex()); // Light grey separator
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataCellStyle.setBorderTop(BorderStyle.THIN); // Add top border for consistency
        dataCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());


        // Style for Notes Column (requires text wrapping)
        CellStyle notesCellStyle = workbook.createCellStyle();
        notesCellStyle.cloneStyleFrom(dataCellStyle); // Start with data cell style
        notesCellStyle.setWrapText(true); // Enable text wrapping for notes


        // Style for Date/Time Column (can add date format if needed)
        // CellStyle dateCellStyle = workbook.createCellStyle();
        // dateCellStyle.cloneStyleFrom(dataCellStyle);
        // CreationHelper createHelper = workbook.getCreationHelper();
        // dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm")); // Example date format


        // --- Create the header row ---
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Date & Time", "Patient", "Psychiatrist", "Purpose", "Duration (min)", "Status", "Notes"};
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerCellStyle); // Apply header style
        }
        // Optional: Set header row height
        headerRow.setHeightInPoints(20); // Height in points (1 point = 1/72 inch)


        // --- Populate the data rows ---
        int rowNum = 1;
        // Optional: Set a default row height for data rows
        // sheet.setDefaultRowHeightInPoints(15); // Applies to all rows unless overridden


        for (Consultation consultation : consultations) {
            Row row = sheet.createRow(rowNum++);

            // Apply data cell style to all cells in this row
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(dataCellStyle); // Default data style
            }

            // Set cell values and apply specific styles where needed
            row.getCell(0).setCellValue(consultation.getId());
            row.getCell(1).setCellValue(consultation.getConsultationTime() != null ? consultation.getConsultationTime().toString() : "N/A"); // Use a format if needed
            row.getCell(2).setCellValue(consultation.getPatientName());
            row.getCell(3).setCellValue(consultation.getPsychiatristName());
            row.getCell(4).setCellValue(consultation.getPurpose() != null ? consultation.getPurpose() : "");
            // Handle duration as a number or string
            if (consultation.getDurationMinutes() > 0) {
                row.getCell(5).setCellValue(consultation.getDurationMinutes()); // Numeric value
            } else {
                row.getCell(5).setCellValue("N/A"); // Text "N/A"
                // Or leave as 0 if you setCellStyle(dataCellStyle) which defaults to numeric
                // row.getCell(5).setCellValue(0);
            }

            row.getCell(6).setCellValue(consultation.getStatusDisplayValue());

            // Apply notes specific style (with wrapping) to the notes cell
            Cell notesCell = row.getCell(7);
            notesCell.setCellValue(consultation.getNotes() != null ? consultation.getNotes() : "");
            notesCell.setCellStyle(notesCellStyle); // Apply the notes style


            // Optional: Adjust row height based on content (especially for notes)
            // This is complex and often requires calculation based on font, width, and content
            // POI doesn't auto-size row height based on wrap text automatically.
            // You can set a fixed height: row.setHeightInPoints(50);

        }

        // --- Final Touches ---

        // Auto-size columns based on content AFTER all data is populated
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Optional: Adjust width of specific columns manually if auto-size is not sufficient
        // sheet.setColumnWidth(1, 20 * 256); // Set Date/Time column width (width in characters * 256)
        // sheet.setColumnWidth(7, 50 * 256); // Set Notes column width to 50 characters wide


        // Optional: Freeze the header row so it's always visible when scrolling
        sheet.createFreezePane(0, 1); // Freeze 0 columns and the first row (row 0)


        // Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            // Close the workbook to free resources
            workbook.close();
        }
    }

    // No changes needed for other potential generic methods if you add them
    /*
    public static <T> void exportDataToExcel(List<T> data, String sheetName, String[] headers, ValueExtractor<T>[] valueExtractors, String filePath) throws IOException {
       // More complex generic export logic
    }

    interface ValueExtractor<T> {
        Object getValue(T item);
    }
    */
}