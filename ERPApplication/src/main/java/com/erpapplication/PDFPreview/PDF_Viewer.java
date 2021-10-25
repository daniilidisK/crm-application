package com.erpapplication.PDFPreview;

import org.icepdf.ri.common.MyAnnotationCallback;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import javax.swing.*;

public class PDF_Viewer {
    public static void main(String args) {
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JPanel viewerComponentPanel = factory.buildViewerPanel();

        controller.getDocumentViewController().setAnnotationCallback(new MyAnnotationCallback(controller.getDocumentViewController()));

        JFrame applicationFrame = new JFrame();
        applicationFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        applicationFrame.getContentPane().add(viewerComponentPanel);
        applicationFrame.setSize(500, 500);
        applicationFrame.setLocation(400, 50);

        controller.openDocument(args);
        JToggleButton btn = new JToggleButton();
        controller.setPageViewSinglePageNonConButton(btn);

        applicationFrame.pack();
        applicationFrame.setVisible(true);
    }
}