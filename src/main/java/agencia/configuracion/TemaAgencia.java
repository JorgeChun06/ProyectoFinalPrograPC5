/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agencia.configuracion;

/**
 *
 * @author Jorge Chun
 */


import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class TemaAgencia {
    
    public static void aplicar() {
        try {
           
            FlatDarkLaf.setup();
            
           
            
            
            Color colorFondo = new Color(30, 30, 30);
         
            Color colorBotones = new Color(0, 120, 215);        
            Color colorBotonesHover = new Color(52, 152, 219);
            
            Color colorCajasTexto = new Color(45, 45, 45);      
          
            Color colorTexto = new Color(230, 230, 230);
            
            UIManager.put("Panel.background", colorFondo);
            UIManager.put("RootPane.background", colorFondo);
            UIManager.put("OptionPane.background", colorFondo); 
            UIManager.put("OptionPane.messageForeground", colorTexto);
           
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            
            UIManager.put("Button.background", colorBotones);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", colorBotonesHover);
            
            UIManager.put("TextField.background", colorCajasTexto);
            UIManager.put("TextField.foreground", colorTexto);
            UIManager.put("ComboBox.background", colorCajasTexto);
            UIManager.put("ComboBox.foreground", colorTexto);
            UIManager.put("Component.focusedBorderColor", colorBotones);
            
            UIManager.put("Label.foreground", colorTexto);
           
            Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 14);
            Font fuenteTitulos = new Font("Segoe UI", Font.BOLD, 15);
            
            UIManager.put("defaultFont", fuenteGeneral);
            UIManager.put("Button.font", fuenteTitulos);
            
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo cargar el tema visual Dark Mode.");
        }
    }
}