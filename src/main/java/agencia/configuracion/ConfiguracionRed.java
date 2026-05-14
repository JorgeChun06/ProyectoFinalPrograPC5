package agencia.configuracion;

/**
 *
 * @author Jorge Chun
 */

public class ConfiguracionRed {
    private String ipServidor;
    private int puerto;

    public ConfiguracionRed() {
        
        this.ipServidor = "192.168.1.21";
        this.puerto = 5000;
    }

    public String getIpServidor() { return ipServidor; }
    public int getPuerto() { return puerto; }
}