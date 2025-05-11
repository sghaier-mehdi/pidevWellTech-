package com.welltech.util;

import com.welltech.model.Consultation;
import com.welltech.model.Consultation.ConsultationStatus; // Need ConsultationStatus enum

// === Apache POI Imports for Styling and Formatting ===
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For .xlsx format
// For date formatting in cells
import org.apache.poi.ss.util.CellUtil; // Useful for getting/creating cells safely and applying styles
import org.apache.poi.ss.usermodel.CreationHelper; // For date data format

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter; // For formatting LocalDateTime to String for N/A
import java.util.List;
import java.util.Map; // For storing status styles
import java.util.HashMap; // For storing status styles

public class ExcelExporter {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Format for displaying dates as strings

    /**
     * Exports a list of Consultations to an Excel (.xlsx) file with enhanced styling.
     *
     * @param consultations List of consultations to export.
     * @param filePath      The full path including filename where the Excel file will be saved.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void exportConsultationsToExcel(List<Consultation> consultations, String filePath) throws IOException {
        System.out.println("ExcelExporter: Starting export to Excel: " + filePath); // Debug

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Consultations");

        // --- Create Styles ---

        // Font for Header
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex()); // White text

        // Cell Style for Header Row
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); // Dark blue background for header
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // Borders
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM); // Thicker bottom border for header
        headerCellStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());


        // --- Styles for Data Rows (Alternating Colors) ---
        CellStyle evenRowStyle = workbook.createCellStyle();
        evenRowStyle.setAlignment(HorizontalAlignment.LEFT);
        evenRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        evenRowStyle.setBorderBottom(BorderStyle.THIN);
        evenRowStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        evenRowStyle.setBorderLeft(BorderStyle.THIN);
        evenRowStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        evenRowStyle.setBorderRight(BorderStyle.THIN);
        evenRowStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        evenRowStyle.setBorderTop(BorderStyle.THIN);
        evenRowStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        evenRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex()); // White background for even rows
        evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle oddRowStyle = workbook.createCellStyle();
        oddRowStyle.cloneStyleFrom(evenRowStyle); // Copy initial style
        oddRowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex()); // Light yellow background for odd rows
        oddRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // --- Style for Notes Column (with wrapping) ---
        CellStyle notesStyleEven = workbook.createCellStyle();
        notesStyleEven.cloneStyleFrom(evenRowStyle);
        notesStyleEven.setWrapText(true);

        CellStyle notesStyleOdd = workbook.createCellStyle();
        notesStyleOdd.cloneStyleFrom(oddRowStyle);
        notesStyleOdd.setWrapText(true);


        // --- Styles for Status Column (Color-coded by Status) ---
        Map<ConsultationStatus, CellStyle> statusStyles = new HashMap<>();
        Font statusFont = workbook.createFont(); // Optional: A different font for status
        statusFont.setBold(true);
        statusFont.setFontHeightInPoints((short) 10); // Smaller font for status

        // Helper to create a basic status style
        java.util.function.Function<IndexedColors, CellStyle> createStatusStyle = (color) -> {
            CellStyle style = workbook.createCellStyle();
            style.setFont(statusFont);
            style.setFillForegroundColor(color.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER); // Center status text
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setBorderBottom(BorderStyle.THIN); style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderLeft(BorderStyle.THIN); style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderRight(BorderStyle.THIN); style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderTop(BorderStyle.THIN); style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            return style;
        };

        // Define styles for each status color (use colors that visually align with your app's CSS)
        statusStyles.put(ConsultationStatus.SCHEDULED, createStatusStyle.apply(IndexedColors.SKY_BLUE)); // Light blue for Scheduled
        statusStyles.put(ConsultationStatus.COMPLETED, createStatusStyle.apply(IndexedColors.LIGHT_GREEN)); // Light green for Completed
        statusStyles.put(ConsultationStatus.CANCELLED, createStatusStyle.apply(IndexedColors.ROSE)); // Light red for Cancelled
        statusStyles.put(ConsultationStatus.RESCHEDULED, createStatusStyle.apply(IndexedColors.LIGHT_ORANGE)); // Light orange for Rescheduled
        // Default style for status if not matched
        CellStyle defaultStatusStyle = createStatusStyle.apply(IndexedColors.GREY_25_PERCENT);


        // --- Create the header row ---
        Row headerRow = sheet.createRow(0);
        // === Headers - Removed "ID" ===
        String[] headers = {"Date & Time", "Patient", "Psychiatrist", "Purpose", "Duration (min)", "Status", "Notes"};
        // ==============================

        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerCellStyle);
        }
        headerRow.setHeightInPoints(25); // Increased header row height


        // --- Populate the data rows ---
        int rowNum = 1;
        // Optional: Set a default row height for data rows (especially since notes wrap)
        sheet.setDefaultRowHeightInPoints(40); // Default to a taller row


        for (Consultation consultation : consultations) {
            Row row = sheet.createRow(rowNum++);

            // Determine which row style to use (even or odd)
            CellStyle rowBaseStyle = (row.getRowNum() % 2 == 0) ? evenRowStyle : oddRowStyle;
            CellStyle rowNotesStyle = (row.getRowNum() % 2 == 0) ? notesStyleEven : notesStyleOdd;


            // --- Populate cells and apply styles ---
            // Cell 0: Date & Time
            Cell cellDateTime = row.createCell(0);
            cellDateTime.setCellValue(consultation.getConsultationTime() != null ? consultation.getConsultationTime().format(DISPLAY_DATE_FORMATTER) : "N/A");
            cellDateTime.setCellStyle(rowBaseStyle); // Apply base style


            // Cell 1: Patient
            Cell cellPatient = row.createCell(1);
            cellPatient.setCellValue(consultation.getPatientName());
            cellPatient.setCellStyle(rowBaseStyle);


            // Cell 2: Psychiatrist
            Cell cellPsychiatrist = row.createCell(2);
            cellPsychiatrist.setCellValue(consultation.getPsychiatristName());
            cellPsychiatrist.setCellStyle(rowBaseStyle);


            // Cell 3: Purpose
            Cell cellPurpose = row.createCell(3);
            cellPurpose.setCellValue(consultation.getPurpose() != null ? consultation.getPurpose() : "");
            cellPurpose.setCellStyle(rowBaseStyle);


            // Cell 4: Duration (min)
            Cell cellDuration = row.createCell(4);
            if (consultation.getDurationMinutes() > 0) {
                cellDuration.setCellValue(consultation.getDurationMinutes()); // Numeric value
            } else {
                cellDuration.setCellValue("N/A"); // Text "N/A"
            }
            cellDuration.setCellStyle(rowBaseStyle);


            // Cell 5: Status
            Cell cellStatus = row.createCell(5);
            String statusDisplay = consultation.getStatusDisplayValue();
            cellStatus.setCellValue(statusDisplay);
            // Apply status-specific style
            CellStyle statusSpecificStyle = statusStyles.getOrDefault(consultation.getStatus(), defaultStatusStyle);
            // Clone the status specific style and apply the base row style's borders to it for consistency
            CellStyle finalStatusStyle = workbook.createCellStyle();
            finalStatusStyle.cloneStyleFrom(statusSpecificStyle);
            finalStatusStyle.setBorderBottom(rowBaseStyle.getBorderBottom()); finalStatusStyle.setBottomBorderColor(rowBaseStyle.getBottomBorderColor());
            finalStatusStyle.setBorderLeft(rowBaseStyle.getBorderLeft()); finalStatusStyle.setLeftBorderColor(rowBaseStyle.getLeftBorderColor());
            finalStatusStyle.setBorderRight(rowBaseStyle.getBorderRight()); finalStatusStyle.setRightBorderColor(rowBaseStyle.getRightBorderColor());
            finalStatusStyle.setBorderTop(rowBaseStyle.getBorderTop()); finalStatusStyle.setTopBorderColor(rowBaseStyle.getTopBorderColor());
            cellStatus.setCellStyle(finalStatusStyle); // Apply the final combined style


            // Cell 6: Notes
            Cell cellNotes = row.createCell(6);
            cellNotes.setCellValue(consultation.getNotes() != null ? consultation.getNotes() : "");
            cellNotes.setCellStyle(rowNotesStyle); // Apply the notes style (with wrapping)


            // Optional: Manually adjust row height based on notes content if needed
            // String notes = consultation.getNotes() != null ? consultation.getNotes() : "";
            // int estimatedLines = (notes.length() / 50) + 1; // Very rough estimate
            // row.setHeightInPoints(estimatedLines * sheet.getDefaultRowHeightInPoints() / 15); // Adjust based on default row height
        }

        // --- Final Touches ---

        // Auto-size columns based on content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Optional: Manually adjust width of specific columns if auto-size is not sufficient
        // sheet.setColumnWidth(0, 18 * 256); // Date/Time
        // sheet.setColumnWidth(3, 30 * 256); // Purpose
        // sheet.setColumnWidth(6, 60 * 256); // Notes column (needs more width due to wrapping)

        // Optional: Freeze the header row
        sheet.createFreezePane(0, 1);


        // Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            System.out.println("ExcelExporter: Export successful: " + filePath); // Debug
        } finally {
            // Close the workbook to free resources
            workbook.close();
            System.out.println("ExcelExporter: Workbook closed."); // Debug
        }
    }

    // No changes needed for other potential generic methods
}