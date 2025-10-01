package com.smartrent.Controller; // Or your preferred package

import com.smartrent.Model.Tenant.PaymentData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ReceiptService {

    /**
     * Generates a PDF rent receipt for a given payment.
     * @param payment The PaymentData object containing receipt details.
     * @param filePath The full path where the PDF file will be saved.
     * @throws IOException If there is an error writing the file.
     */
    public void generateReceipt(PaymentData payment, String filePath) throws IOException {
        // Create a new, empty PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // Prepare a content stream to write to the page
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // --- Start Writing Content ---

        // Title
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
        contentStream.newLineAtOffset(230, 750);
        contentStream.showText("Rent Receipt");
        contentStream.endText();

        // Line Separator
        contentStream.setStrokingColor(java.awt.Color.LIGHT_GRAY);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, 730);
        contentStream.lineTo(550, 730);
        contentStream.stroke();

        // Receipt Details
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String paymentDate = sdf.format(payment.getPaymentDate().toDate());
        String receiptId = "RCPT-" + System.currentTimeMillis();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.setLeading(14.5f); // Set line spacing
        contentStream.newLineAtOffset(70, 680);
        contentStream.showText("Receipt No: " + receiptId);
        contentStream.newLine();
        contentStream.showText("Payment Date: " + paymentDate);
        contentStream.endText();

        // Payment Information Table
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.newLineAtOffset(70, 600);
        contentStream.showText("Description");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.newLineAtOffset(450, 600);
        contentStream.showText("Amount");
        contentStream.endText();

        // Line Separator for table header
        contentStream.moveTo(50, 590);
        contentStream.lineTo(550, 590);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(70, 560);
        contentStream.showText("Monthly Rent Payment");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(450, 560);
        contentStream.showText("Rs. " + String.format("%,.2f", payment.getRentAmount()));
        contentStream.endText();

        // Total Amount
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.newLineAtOffset(350, 500);
        contentStream.showText("Total Paid:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.newLineAtOffset(450, 500);
        contentStream.showText("Rs. " + String.format("%,.2f", payment.getRentAmount()));
        contentStream.endText();

        // --- MODIFIED: Received From section ---
        String propertyInfo = "Property: " + (payment.getSocietyName() != null ? payment.getSocietyName() : "N/A") + 
                              ", Flat " + (payment.getFlatNo() != null ? payment.getFlatNo() : "N/A");

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(70, 400);
        contentStream.showText("Received from:");
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLine();
        contentStream.showText(payment.getTenantName());
        contentStream.newLine();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.showText(payment.getTenantEmail());
        contentStream.newLine();
        contentStream.showText(propertyInfo); // Added property details
        contentStream.newLine();
        contentStream.showText("Payment Method: UPI (" + payment.getUpiId() + ")");
        contentStream.endText();

        // Footer
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
        contentStream.newLineAtOffset(220, 100);
        contentStream.showText("Thank you for your timely payment!");
        contentStream.endText();


        // --- End Writing Content ---

        // Close the content stream
        contentStream.close();

        // Save the document and close it
        document.save(filePath);
        document.close();

        System.out.println("Receipt generated successfully at: " + filePath);
    }
}
