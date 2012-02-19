package com.axmor.redmine.uploader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
  private static final long serialVersionUID = 5631821278779135825L;
  private BufferedImage image;

  public ImagePanel()
  {
    super(true);
  }

  public BufferedImage getImage()
  {
    return this.image;
  }

  public void setImage(BufferedImage image)
  {
    this.image = image;
    if (image != null) {
      setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }
    updateUI();
  }

  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if (this.image != null)
      g.drawImage(this.image, 0, 0, this);
  }
}