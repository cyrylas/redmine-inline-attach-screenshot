package com.axmor.redmine.uploader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class Uploader extends JApplet
{
  private static final long serialVersionUID = -935122463834512061L;
  private static final String newline = "\r\n";
  private static final String boundary = Long.toHexString(new Random().nextLong());
  private static final String ENCODING = "utf-8";
  private ImagePanel canvas;
  private JButton attachButton;

  /**
   * Initialization of the uploader applet.
   */
  public void init() {
    setSize(400, 400);
    this.canvas = new ImagePanel();
    this.canvas.setBackground(Color.WHITE);
    
    JScrollPane imageScrollPane = new JScrollPane(this.canvas, 20, 30);
    imageScrollPane.setBorder(new TitledBorder(getParameter("label.image", "Image")));

    JButton pasteButton = new JButton(getParameter("label.button.paste", "Paste image from clipboard"));
    pasteButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        Uploader.this.pasteImageFromClipboard();
      }
    });
    
    this.attachButton = new JButton(getParameter("label.button.attach", "Attach"));
    this.attachButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Uploader.this.attachImage();
      }
    });
    
    JButton cancelButton = new JButton(getParameter("label.button.cancel", "cancel"));
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Uploader.this.closeApplet();
      }
    });

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, 2));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(pasteButton);
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(this.attachButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(cancelButton);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 5), true);
    mainPanel.add(imageScrollPane, "Center");
    mainPanel.add(buttonPane, "South");
    getContentPane().add(mainPanel);

    // Automatically paste image from clipboard if it's already on there.
    pasteImageFromClipboard();
  }

  /**
   * Uploads whatever image is loaded on the canvas to the Redmine server as per attach.url parameter.
   */
  private void attachImage() {
    try {
      String fileId = sendContentToServer(getParameter("attach.url", "http://192.168.10.10/redmine_dev/attach_screenshot"), this.canvas.getImage());

      getAppletContext().showDocument(new URL("javascript:addAttachScreen('" + fileId + "');"));

      closeApplet();
    } catch (IOException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, MessageFormat.format(getParameter("error.attach.msg", "Error: {0}"), new Object[] { e.getMessage() }), getParameter("error.attach.title", "Attaching error"), 0);
    }
  }

  /**
   * HTTP upload of BufferedImage image.
   * @param url The URL to POST the image to.
   * @param image The image to upload.
   * @return The file ID that our Redmine plugin saved this temporary image as.
   * @throws IOException
   */
  private String sendContentToServer(String url, BufferedImage image)
    throws IOException {
    URL destURL = new URL(url);
    HttpURLConnection urlConn = (HttpURLConnection)destURL.openConnection();
    urlConn.setRequestMethod("POST");
    urlConn.setDoOutput(true);
    urlConn.setDoInput(true);
    urlConn.setUseCaches(false);
    urlConn.setAllowUserInteraction(false);
    urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    urlConn.setRequestProperty("User-Agent", "Redmine Screenshot Attach Applet");

    OutputStream outStream = urlConn.getOutputStream();
    try {
      outStream.write(("--" + boundary + newline).getBytes(ENCODING));
      outStream.write(("Content-Disposition: form-data; name=\"attachments\"; filename=\"screenshot.png\"" + newline).getBytes(ENCODING)); 
      outStream.write(("Content-Type: image/png" + newline + newline).getBytes(ENCODING));
      ImageIO.write(image, "png", outStream);
      outStream.write((newline + "--" + boundary +"--").getBytes(ENCODING));
    } finally {
      outStream.close();
    }
    
    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
    try {
      String str = in.readLine();
      return str; 
    } finally { 
    	in.close();
    }
  }

  /**
   * Closes the applet.
   */
  private void closeApplet() {
    try {
      getAppletContext().showDocument(new URL("javascript:hideAttachScreen();"));
    }
    catch (MalformedURLException e) {
      JOptionPane.showMessageDialog(null, MessageFormat.format(getParameter("error.close.msg", "Can not close applet, {0}"), new Object[] { e.getMessage() }), getParameter("error.close.title", "Error"), 0);
    }
  }

  /**
   * Pulls the image off of the clipboard and loads it into the ImagePanel canvas.
   */
  private void pasteImageFromClipboard()
  {
    this.attachButton.setEnabled(false);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
      Transferable contents = clipboard.getContents(null);
      if (contents != null) {
        try {
          if (contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            this.canvas.setImage((BufferedImage)contents.getTransferData(DataFlavor.imageFlavor));
            this.attachButton.setEnabled(true);
          }
        } catch (UnsupportedFlavorException ufe) {
          ufe.printStackTrace();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   * GetParameter with a fallback onto a default value if unavailable.
   * @param key Key to attempt to retrieve.
   * @param def Default value on failure.
   * @return Value of key if exists, or default.
   */
  private final String getParameter(String key, String def){
    String param = getParameter(key);
    if (param == null) {
      return def;
    }
    return param;
  }
}