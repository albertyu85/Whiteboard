import java.awt.*;
import java.awt.font.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;
  
public class FittingAFont extends JPanel {
    String s = "Hello World";
  
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font font = g2.getFont().deriveFont(36f);
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(s, frc);
        // Try omitting the descent from the height variable.
        float height = metrics.getAscent() + metrics.getDescent();
        double width = font.getStringBounds(s, frc).getWidth();
        int w = getWidth();
        int h = getHeight();
        double xScale = w/width;
        double yScale = (double)h/height;
        double x = (w - xScale*width)/2;
        double y = (h + yScale*height)/2 - yScale*metrics.getDescent();
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.scale(xScale, yScale);
        g2.setFont(font.deriveFont(at));
        g2.drawString(s, 0, 0);
    }
  
    public static void main(String[] args) {
        JFrame f = new JFrame("drag edges");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new FittingAFont());
        f.setSize(175,90);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}