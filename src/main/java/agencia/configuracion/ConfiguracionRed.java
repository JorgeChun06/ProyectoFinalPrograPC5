package agencia.configuracion;

/**
 *
 * @author Jorge Chun
 */



import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfiguracionRed {
    
    private String ipServidor;
    private int puerto;

    public ConfiguracionRed() {
        Properties propiedades = new Properties();
        try (FileInputStream entrada = new FileInputStream("config.properties")) {
            propiedades.load(entrada);
            this.ipServidor = propiedades.getProperty("IP_SERVIDOR", "172.20.10.3");
            String puertoStr = propiedades.getProperty("PUERTO_SERVIDOR", 
                               propiedades.getProperty("PUERTO", "5000"));
            
            this.puerto = Integer.parseInt(puertoStr);
            
        } catch (IOException ex) {
            System.err.println("Advertencia: No se encontró 'config.properties'.");
            System.err.println("Aplicando configuración por defecto de red local (127.0.0.1 : 5000).");
            this.ipServidor = "172.20.10.3";
            this.puerto = 5000;
        } catch (NumberFormatException ex) {
            System.err.println("Error: El puerto definido en config.properties no es un número válido.");
            this.ipServidor = "172.20.10.3";
            this.puerto = 5000;
        }
    }
    
    public String getIpServidor() {
        return ipServidor;
    }

    public int getPuerto() {
        return puerto;
    }
}