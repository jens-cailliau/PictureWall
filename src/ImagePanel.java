import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import javaxt.io.Image;

public class ImagePanel extends JPanel
{
  private BufferedImage image;
  private Image backgroundImage;

  private static final int PANEL_WIDTH = 1920;
  private static final int PANEL_HEIGHT= 1080;

  private static final int IMAGEPANEL_WIDTH = 763;
  private static final int IMAGEPANEL_HEIGHT = 763;
  private static final int IMAGEPANEL_X = 573;
  private static final int IMAGEPANEL_Y = 150;

  private static final int PADDING = 10;

  private static final int TEXTPANEL_X = 1390;
  private static final int TEXTPANEL_Y = 309;

  private static final String TEXT_1 = "Upload uw foto op: \njhdemoane.be/upload";
  private static final String TEXT_2 = "Copyright Jens Cailliau";

  public ImagePanel()
  {
    super();

    JFrame myFrame = new JFrame();
    myFrame.add(this);
    myFrame.pack();
    myFrame.setVisible(true);

    backgroundImage = new javaxt.io.Image("3jrdemoane 3.png");

    myFrame.setSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);

    // clear screen.
    pGraphics.clearRect(0, 0, getWidth(), getHeight());

    Graphics2D g2d = (Graphics2D) pGraphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // show picture.
    if (image != null)
    {
      // center image
      int x = (IMAGEPANEL_WIDTH - image.getWidth(null)) / 2 + IMAGEPANEL_X;
      int y = (IMAGEPANEL_HEIGHT - image.getHeight(null)) / 2 + IMAGEPANEL_Y;

      // draw image
      g2d.drawImage(image, x, y, null);
    }

    // draw background image.
    if (backgroundImage != null)
    {
      g2d.drawImage(backgroundImage.getBufferedImage(), 0, 0, null);
    }

    // set background color for text background
    //g2d.setColor(Color.RED);

    // draw text background
    //g2d.fillRect(20, 20, 200, 50);
    //g2d.fillRect(20, PANEL_HEIGHT - 90, 250, 50);
    //g2d.fillRect(PANEL_WIDTH / 4 * 3, 20, 400, 50);

    // set background color for text
    g2d.setColor(Color.WHITE);

    // draw text (queue / pending counts)
    g2d.setFont(new Font("Calibri Light", Font.BOLD, 36));

    // upload info
    drawString(g2d, TEXT_1, TEXTPANEL_X, TEXTPANEL_Y);

    // queue info
    String queueInfo = "Afspeellijst : " + Main.getCurrentPicture() + " / " + Main.getQueueCount() + "\n" + "Wachtrij : " + Main.getPendingCount();
    drawString(g2d, queueInfo, TEXTPANEL_X, TEXTPANEL_Y + 100);

    // copyright info
    drawString(g2d, TEXT_2, TEXTPANEL_X, TEXTPANEL_Y + 200);
  }

  void drawString(Graphics pGraphics, String pText, int pX, int pY) {
    for (String line : pText.split("\n"))
      pGraphics.drawString(line, pX, pY += pGraphics.getFontMetrics().getHeight());
  }

  public void setImage(javaxt.io.Image pImage)
  {
    // show picture.
    if (pImage == null) return;

    image = pImage.getBufferedImage();
    repaint();
  }

  public int getPanelWidth()
  {
    return PANEL_WIDTH;
  }

  public int getPanelHeight()
  {
    return PANEL_HEIGHT;
  }

  public int getImageMaxHeight()
  {
    return IMAGEPANEL_HEIGHT - PADDING;
  }

  public int getImageMaxWidth()
  {
    return IMAGEPANEL_WIDTH - PADDING;
  }
}