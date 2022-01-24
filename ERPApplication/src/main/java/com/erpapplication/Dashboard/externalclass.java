package com.erpapplication.Dashboard;

import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;
import java.io.IOException;
import java.io.FileOutputStream;
import java.lang.String;
import java.util.NoSuchElementException;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.erpapplication.products.TableData;
import com.erpapplication.products.productController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

public class externalclass extends Component {
    static double clearAmount;
    public static ObservableList<Invoice> invoice_items = FXCollections.observableArrayList();

    public externalclass(int InvNo,
                         LocalDate Date,
                         String Client,
                         String Address,
                         String City,
                         String Doy,
                         String Occupation,
                         double VatNo,
                         String VAT_ID,
                         String Payment) throws IOException, DocumentException {

        ArrayList<Invoice> payables = new ArrayList<>();

        payables.add(new Invoice(InvNo, Date, Client, Address, City, Doy, Occupation, VatNo, VAT_ID, Payment));
        Iterator<Invoice> iterator = payables.iterator();

        //PDF creation
        Document document = new Document(PageSize.A4);
        BaseFont fonty1 = BaseFont.createFont("com/erpapplication/fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fonty1.setSubset(true);

        Font font1 = new Font(fonty1, 12.0f, Font.NORMAL, BaseColor.BLACK);
        Font font2 = new Font(fonty1, 12.0f, Font.BOLD, BaseColor.BLACK);
        Font font3 = new Font(fonty1, 14.0f, Font.BOLD, BaseColor.BLACK);

        try {
            // File Chooser Window - Save Invoice
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
            fileChooser.setInitialFileName(Date + "_ΤΠΥ_" + InvNo + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fileChooser.setTitle("Save Invoice");
            File file = fileChooser.showSaveDialog(null);
            //Show file path
            //path = file.getCanonicalPath();
            //System.out.println(file.getCanonicalPath());

            // Output .pdf file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            writer.setCompressionLevel(9);

            // Work finished message
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Έκδοση Τιμολογίου");
            a.setHeaderText("Εκδόθηκε τιμολόγιο");
            a.setContentText("Το τιμολόγιο " + InvNo + " αποθηκεύτηκε στην τοποθεσία: " + file.getCanonicalPath());
            a.showAndWait();

            // Writing of the Invoice
            document.open();

            InitializeDB.changeDatabase("AADE", "Settings");

            String logo = "";
            for (org.bson.Document oldDoc : InitializeDB.collection.find())
                logo = (oldDoc.getString("Logo"));

            Image img = Image.getInstance(logo);
            img.setAbsolutePosition(0, 700);
            img.scaleAbsolute(130, 110);
            document.add(img);

            // Print Details
            while (iterator.hasNext()) {

                // Enterprise's Details
                String[] EnterpriseDetails = {
                        """
Diagnosis Multisystems IKE
Ηλεκτρομηχανολογικές Μελέτες
ΑΦΜ: 801166478
No Γ.Ε.Μ.Η.: 150620306000
Ανατολικής Θράκης 2, Καλαμαριά, 55134"""
                };

                StringBuilder EnterpriseInfo = new StringBuilder();
                for (Object value : EnterpriseDetails) {
                    EnterpriseInfo.append(value);
                }
                String Enterprise = EnterpriseInfo.toString();

                Paragraph p1 = new Paragraph(Enterprise, font2);
                p1.setAlignment(Element.ALIGN_RIGHT);
                document.add(p1);

                // Invoice
                Paragraph P = new Paragraph("\nΤΙΜΟΛΟΓΙΟ", font3);
                P.setAlignment(Element.ALIGN_CENTER);
                document.add(P);

                // Invoice Number and Date
                document.add(Chunk.NEWLINE);
                Paragraph p2 = new Paragraph("Νο Τιμολογίου: " + InvNo, font1);
                p2.setAlignment(Element.ALIGN_RIGHT);
                document.add(p2);

                // Date format
                DateTimeFormatter DateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                Paragraph p3 = new Paragraph("Ημερομηνία: " + Date.format(DateFormat), font1);
                p3.setAlignment(Element.ALIGN_RIGHT);
                document.add(p3);

                // Client Details
                Paragraph p4 = new Paragraph("Στοιχεία Πελάτη: ", font3);
                p4.setAlignment(Element.ALIGN_LEFT);
                document.add(p4);

                Paragraph p5 = new Paragraph(iterator.next().toString(), font1);
                p5.setAlignment(Element.ALIGN_LEFT);
                document.add(p5);

                document.add(Chunk.NEWLINE);

                // Invoice Table
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(107);
                table.setWidths(new int[]{8,2,2,2});

                table.addCell(createCell("ΠΕΡΙΓΡΑΦΗ", Element.ALIGN_CENTER, new BaseColor(222,222,222)));
                table.addCell(createCell("ΠΟΣΟ- ΤΗΤΑ", Element.ALIGN_CENTER, new BaseColor(222,222,222)));
                table.addCell(createCell("ΤΙΜΗ ΜΟΝΑΔΑΣ", Element.ALIGN_CENTER, new BaseColor(222,222,222)));
                table.addCell(createCell("ΑΞΙΑ", Element.ALIGN_CENTER, new BaseColor(222,222,222)));

                final ObservableList<TableData> list = productController.list_items;

                String[] data;
                clearAmount = 0;
                for (TableData o:list) {
                    data = new String[]{o.getDesc(), String.valueOf(o.getQuantity()), "€ " + o.getItem_price(), "€ " + String.format("%.2f", o.getQuantity()*o.getItem_price())};
                    clearAmount += o.getQuantity() * o.getItem_price();

                    for (String row:data) {
                        if (row.equals(o.getDesc()))
                            table.addCell(createCell(row, Element.ALIGN_LEFT, BaseColor.WHITE));

                        else table.addCell(createCell(row, Element.ALIGN_RIGHT, BaseColor.WHITE));
                    }
                }

                table.addCell(EmptyCell());
                table.addCell(EmptyCell());
                table.addCell(FinalNoteCells("ΚΑΘΑΡΗ ΑΞΙΑ"));
                table.addCell(createCell("€ " + String.format("%.2f", (clearAmount)), Element.ALIGN_RIGHT, BaseColor.WHITE));

                table.addCell(EmptyCell());
                table.addCell(EmptyCell());
                table.addCell(FinalNoteCells("Φ.Π.Α. " + VatNo + "%"));
                table.addCell(createCell("€ " + String.format("%.2f", (clearAmount * VatNo/100.0)), Element.ALIGN_RIGHT, BaseColor.WHITE));

                table.addCell(PaymentCell(Payment, Element.ALIGN_CENTER));
                table.addCell(EmptyCell());
                table.addCell(FinalNoteCells("ΣΥΝΟΛΟ"));
                table.addCell(createCell("€ " + String.format("%.2f", (clearAmount * ((VatNo/100.0) + 1))), Element.ALIGN_RIGHT, new BaseColor(222,222,222)));

                // Footer Table Title
                PdfPTable tfooter = new PdfPTable(3);
                tfooter.setWidths(new int[]{10, 300, 10});
                tfooter.setTotalWidth(500);
                tfooter.setLockedWidth(false);
                tfooter.getDefaultCell().setFixedHeight(50);
                tfooter.getDefaultCell().setBorder(Rectangle.BOTTOM);
                tfooter.getDefaultCell().setBackgroundColor(BaseColor.WHITE);

                // Bank Account Footer Table
                PdfPTable footer = new PdfPTable(2);
                footer.setWidths(new int[]{130, 130});
                footer.setTotalWidth(500);
                footer.setLockedWidth(false);
                footer.getDefaultCell().setFixedHeight(60);
                footer.getDefaultCell().setBorder(Rectangle.BOTTOM);
                footer.getDefaultCell().setBorderColor(BaseColor.WHITE);

                // Footer Title
                tfooter.addCell(EmptyCell());
                tfooter.addCell(FooterCell("---------------------------------- ΠΛΗΡΟΦΟΡΙΕΣ ΠΛΗΡΩΜΗΣ ----------------------------------", Element.ALIGN_CENTER));
                tfooter.addCell(EmptyCell());
                // Footer Table Cells
                footer.addCell(FooterCell("ΤΡΑΠΕΖΑ: ΕΘΝΙΚΗ ΤΡΑΠΕΖΑ ΤΗΣ ΕΛΛΑΔΟΣ", Element.ALIGN_LEFT));
                footer.addCell(FooterCell("IBAN: GR85 0110 4210 0000 4212 2203 856", Element.ALIGN_LEFT));
                footer.addCell(FooterCell("ΤΡΑΠΕΖΑ: ΤΡΑΠΕΖΑ ΠΕΙΡΑΙΩΣ", Element.ALIGN_LEFT));
                footer.addCell(FooterCell("IBAN: GR47 0171 2320 0062 3214 7753 799", Element.ALIGN_LEFT));
                footer.addCell(EmptyCell());

                // Footer Title write in page
                PdfContentByte tcanvas = writer.getDirectContent();
                tcanvas.beginMarkedContentSequence(PdfName.ARTIFACT);
                tfooter.writeSelectedRows(0, -1, 40, 80, tcanvas);
                tcanvas.endMarkedContentSequence();

                // Footer write in page
                PdfContentByte canvas = writer.getDirectContent();
                canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
                footer.writeSelectedRows(0,-1,40,60, canvas);
                canvas.endMarkedContentSequence();

                // Document Addition and Close
                document.add(table);

                invoice_items.clear();
                invoice_items.add(new Invoice(InvNo, Date, Client, Address, City, Doy, Occupation, VatNo, VAT_ID, Payment, clearAmount));
                // CreateXMLFiles.main(null);
            }
        } catch (DocumentException | IOException | NoSuchElementException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText(e.getMessage());
            e.printStackTrace();
            a.showAndWait();
        } catch(NullPointerException ignored) {
        }
        document.close();
    }

    private static PdfPCell createCell(String content, int alignment, BaseColor color) throws IOException, DocumentException {
        BaseFont fonty2 = BaseFont.createFont("com/erpapplication/fonts/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fonty2.setSubset(true);
        Font defaultfont = new Font(fonty2, 12f, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cell = new PdfPCell(new Phrase(content, defaultfont));
        cell.setColspan(1);
        cell.setPadding(7);
        cell.setLeading(1f, 1.1f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(color);
        return cell;
    }

    private static PdfPCell EmptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorderWidth(0);
        cell.setBackgroundColor(BaseColor.WHITE);
        return cell;
    }

    private static PdfPCell FinalNoteCells(String content) throws IOException, DocumentException {
        BaseFont fonty3 = BaseFont.createFont("com/erpapplication/fonts/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fonty3.setSubset(true);
        Font defaultfont = new Font(fonty3, 12f, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cell = new PdfPCell(new Phrase(content, defaultfont));
        cell.setBorderWidth(0);
        cell.setPadding(7);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private static PdfPCell FooterCell(String content, int alignment) throws IOException, DocumentException {
        BaseFont fonty4 = BaseFont.createFont("com/erpapplication/fonts/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fonty4.setSubset(true);
        Font defaultfont = new Font(fonty4, 10.0f, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cell = new PdfPCell(new Phrase(content, defaultfont));
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setColspan(1);
        return cell;
    }

    private static PdfPCell PaymentCell(String content, int alignment) throws IOException, DocumentException {
        BaseFont fonty5 = BaseFont.createFont("com/erpapplication/fonts/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fonty5.setSubset(true);
        Font defaultfont = new Font(fonty5, 12.0f, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cell = new PdfPCell(new Phrase(content, defaultfont));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(BaseColor.WHITE);
        return cell;
    }

    public static double returnAmount(){
        return clearAmount;
    }

    public static ObservableList<Invoice> getInvoiceData(int InvNo, LocalDate Date, String Client, String Address,
                                                         String City, String Doy,
                                                         String Occupation, double VatNo,
                                                         String VAT_ID, String Payment) {
        final ObservableList<TableData> list = productController.list_items;
        double clearAmount = 0;

        for (TableData o:list)
            clearAmount += o.getQuantity() * o.getItem_price();

        invoice_items.clear();
        invoice_items.add(new Invoice(InvNo, Date, Client, Address, City, Doy, Occupation, VatNo, VAT_ID, Payment, clearAmount));

        return invoice_items;
    }
}