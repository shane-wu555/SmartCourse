package com.sx.backend.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

@Component
public class ChartGenerator {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;

    public String generateLineChart(List<String> labels, List<Float> values) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(values.get(i), "成绩", labels.get(i));
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "成绩变化趋势",
                "日期",
                "成绩",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // 自定义样式
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));

        return convertChartToBase64(chart, WIDTH, HEIGHT);
    }

    private String convertChartToBase64(JFreeChart chart, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle(width, height));
            g2.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("图表生成失败", e);
        }
    }
}
