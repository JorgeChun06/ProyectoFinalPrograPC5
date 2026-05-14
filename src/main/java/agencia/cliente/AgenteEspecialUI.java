package agencia.cliente;

import agencia.modelos.TicketViaje;
import agencia.configuracion.ConfiguracionRed;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Jorge Chun
 */

public class AgenteEspecialUI extends JFrame {

    private JTextField txtAgente, txtNombre, txtApellido;
    private JComboBox<String> cmbMotivo;
    private JTextArea txtComentarios;
    private JLabel lblStatus, lblTicket, lblDpi, lblIndicadorConexion, lblTiempoAtencion, lblEnCola;
    private JButton btnLlamar, btnFinalizar, btnAbrirChat;
    private ConfiguracionRed config;
    private TicketViaje ticketActual;
    private LocalDateTime horaInicioAtencion;
    private Socket socketPrincipal;
    private ObjectOutputStream outPrincipal;
    private ObjectInputStream inPrincipal;
    private Timer cronometro;
    private Timer radarCola;
    private int segundosTranscurridos = 0;
    
    private boolean estaAtendiendo = false;
    private int clientesEnCola = 0;

    private final Color COLOR_FONDO = new Color(15, 15, 18);
    private final Color COLOR_TARJETA = new Color(25, 25, 30);
    private final Color COLOR_MUTED = new Color(130, 130, 140);
    private final Color AZUL_NEON = new Color(0, 170, 255);
    private final Color ROJO_ALERTA = new Color(255, 70, 85);
    private final Color VERDE_EXITO = new Color(0, 210, 100);
    private final Color NARANJA_TIMER = new Color(255, 165, 0);
    private final Color AZUL_CELESTE = new Color(0, 230, 255);

    public AgenteEspecialUI() {
        config = new ConfiguracionRed();
        configurarVentanaPremium();
        conectarAlDashboard();
        iniciarRadarCola();
    }

    private void configurarVentanaPremium() {
        setTitle("TERMINAL ESPECIAL - PC5 - Ingeniero");
        setSize(900, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    
                    if (outPrincipal != null) {
                        outPrincipal.writeObject("DESCONEXION_REAL");
                        outPrincipal.flush();
                    }
                   
                    if (socketPrincipal != null && !socketPrincipal.isClosed()) {
                        socketPrincipal.close();
                    }
                } catch (Exception ex) {
                  
                }
            }
        });
        JPanel panelMaestro = new JPanel(new BorderLayout(25, 25));
        panelMaestro.setBackground(COLOR_FONDO);
        panelMaestro.setBorder(new EmptyBorder(25, 35, 25, 35));

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);
        panelHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(50, 50, 55))); 

        JPanel headerIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        headerIzquierda.setOpaque(false);
        JLabel lblIconoLogo = new JLabel(" ");
        lblIconoLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblIconoLogo.setForeground(AZUL_NEON);
        JLabel lblTitulo = new JLabel("MODULO DE SERVICIO ESPECIAL");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        headerIzquierda.add(lblIconoLogo);
        headerIzquierda.add(lblTitulo);

        JPanel headerCentro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 13));
        headerCentro.setOpaque(false);
        lblEnCola = new JLabel("COLA: BUSCANDO...");
        lblEnCola.setFont(new Font("Consolas", Font.BOLD, 14));
        lblEnCola.setForeground(COLOR_MUTED);
        headerCentro.add(lblEnCola);

        JPanel headerDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        headerDerecha.setOpaque(false);
        lblIndicadorConexion = new JLabel("");
        lblIndicadorConexion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIndicadorConexion.setForeground(COLOR_MUTED);
        lblStatus = new JLabel("INICIANDO SISTEMA...");
        lblStatus.setFont(new Font("Consolas", Font.BOLD, 14));
        lblStatus.setForeground(COLOR_MUTED);
        headerDerecha.add(lblIndicadorConexion);
        headerDerecha.add(lblStatus);
        panelHeader.add(headerIzquierda, BorderLayout.WEST);
        panelHeader.add(headerCentro, BorderLayout.CENTER);
        panelHeader.add(headerDerecha, BorderLayout.EAST);
        panelMaestro.add(panelHeader, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 30, 0)); 
        panelCentro.setOpaque(false);

        JPanel tarjetaDisplay = new JPanel();
        tarjetaDisplay.setLayout(new BoxLayout(tarjetaDisplay, BoxLayout.Y_AXIS));
        tarjetaDisplay.setBackground(COLOR_TARJETA);
        tarjetaDisplay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 1, 1, 1, AZUL_NEON),
            new EmptyBorder(40, 20, 40, 20)
        ));

        JLabel lblTagDisplay = new JLabel("TICKET EN CURSO");
        lblTagDisplay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTagDisplay.setForeground(AZUL_NEON);
        lblTagDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTicket = new JLabel("---");
        lblTicket.setFont(new Font("Monospaced", Font.BOLD, 75)); 
        lblTicket.setForeground(Color.WHITE); 
        lblTicket.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblTagDpi = new JLabel("DPI DEL PASAJERO");
        lblTagDpi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTagDpi.setForeground(COLOR_MUTED);
        lblTagDpi.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDpi = new JLabel("---");
        lblDpi.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        lblDpi.setForeground(Color.LIGHT_GRAY);
        lblDpi.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTiempoAtencion = new JLabel("⏱ 00:00");
        lblTiempoAtencion.setFont(new Font("Monospaced", Font.BOLD, 40));
        lblTiempoAtencion.setForeground(new Color(50, 50, 50)); 
        lblTiempoAtencion.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjetaDisplay.add(lblTagDisplay);
        tarjetaDisplay.add(Box.createVerticalStrut(20));
        tarjetaDisplay.add(lblTicket);
        tarjetaDisplay.add(Box.createVerticalStrut(30));
        tarjetaDisplay.add(lblTagDpi);
        tarjetaDisplay.add(Box.createVerticalStrut(10));
        tarjetaDisplay.add(lblDpi);
        tarjetaDisplay.add(Box.createVerticalStrut(35));
        tarjetaDisplay.add(lblTiempoAtencion);

        JPanel tarjetaFormulario = new JPanel(new BorderLayout(0, 15));
        tarjetaFormulario.setBackground(COLOR_TARJETA);
        tarjetaFormulario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 1, 1, 1, new Color(80, 80, 90)),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JPanel gridForm = new JPanel(new GridLayout(4, 1, 0, 15));
        gridForm.setOpaque(false);

        txtAgente = crearCampoPro("Agente Asignado:", "Ingeniero", false); 
        JPanel fAgente = agruparCampo("AGENTE OPERATIVO", txtAgente);
        txtNombre = crearCampoPro("Ingrese Nombres", "", false);
        JPanel fNombre = agruparCampo("NOMBRES", txtNombre);
        txtApellido = crearCampoPro("Ingrese Apellidos", "", false);
        JPanel fApellido = agruparCampo("APELLIDOS", txtApellido);
        
        String[] motivosServicio = {
            "Seleccione un motivo...", "Cambio de Fecha / Reprogramacion",
            "Solicitud de Reembolso", "Perdida de Equipaje",
            "Queja sobre Servicio", "Asistencia Especial", "Informacion General"
        };
        cmbMotivo = new JComboBox<>(motivosServicio);
        cmbMotivo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbMotivo.setBackground(new Color(20, 20, 25));
        cmbMotivo.setForeground(Color.WHITE);
        cmbMotivo.setEnabled(false);
        JPanel fMotivo = agruparCampo("MOTIVO DE ATENCION", cmbMotivo);

        gridForm.add(fAgente);
        gridForm.add(fNombre);
        gridForm.add(fApellido);
        gridForm.add(fMotivo);
        txtComentarios = new JTextArea();
        txtComentarios.setEnabled(false);
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        txtComentarios.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtComentarios.setBackground(new Color(20, 20, 25));
        txtComentarios.setForeground(Color.WHITE);
        txtComentarios.setCaretColor(AZUL_NEON);
        txtComentarios.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollComentarios = new JScrollPane(txtComentarios);
        scrollComentarios.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 70)), 
            " NOTAS DEL SERVICIO ", TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 11), COLOR_MUTED));
        scrollComentarios.setBackground(COLOR_TARJETA);
        scrollComentarios.getViewport().setBackground(COLOR_TARJETA);

        tarjetaFormulario.add(gridForm, BorderLayout.NORTH);
        tarjetaFormulario.add(scrollComentarios, BorderLayout.CENTER);

        panelCentro.add(tarjetaDisplay);
        panelCentro.add(tarjetaFormulario);
        panelMaestro.add(panelCentro, BorderLayout.CENTER);

        JPanel panelFooter = new JPanel(new BorderLayout());
        panelFooter.setOpaque(false);
        panelFooter.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAbrirChat = new JButton("ABRIR CHAT ");
        estilizarBoton(btnAbrirChat, new Color(50, 50, 60), Color.WHITE, 160);

        JPanel panelBotonesDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotonesDer.setOpaque(false);

        btnLlamar = new JButton("LLAMAR CLIENTE");
        estilizarBoton(btnLlamar, new Color(50, 50, 60), Color.GRAY, 200);
        btnLlamar.setEnabled(false); 

        btnFinalizar = new JButton("FINALIZAR Y GUARDAR REGISTRO ");
        estilizarBoton(btnFinalizar, VERDE_EXITO, Color.BLACK, 220);
        btnFinalizar.setEnabled(false);

        panelBotonesDer.add(btnLlamar);
        panelBotonesDer.add(btnFinalizar);

        panelFooter.add(btnAbrirChat, BorderLayout.WEST);
        panelFooter.add(panelBotonesDer, BorderLayout.EAST);
        panelMaestro.add(panelFooter, BorderLayout.SOUTH);

        setContentPane(panelMaestro);

        btnAbrirChat.addActionListener(e -> new agencia.cliente.chat.VentanaChat(txtAgente.getText()).setVisible(true));
        btnLlamar.addActionListener(e -> solicitarTicketConcurrente());
        btnFinalizar.addActionListener(e -> finalizarAtencionConcurrente());

        cronometro = new Timer(1000, e -> {
            segundosTranscurridos++;
            int minutos = segundosTranscurridos / 60;
            int segundos = segundosTranscurridos % 60;
            lblTiempoAtencion.setText(String.format("⏱ %02d:%02d", minutos, segundos));
        });
    }

    private JTextField crearCampoPro(String placeholder, String texto, boolean habilitado) {
        JTextField campo = new JTextField(texto);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setBackground(new Color(20, 20, 25));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(AZUL_NEON);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70)),
                new EmptyBorder(8, 12, 8, 12)));
        campo.setEnabled(habilitado);
        return campo;
    }

    private JPanel agruparCampo(String titulo, JComponent campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lblInfo = new JLabel(titulo);
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblInfo.setForeground(COLOR_MUTED);
        panel.add(lblInfo, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg, int ancho) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(ancho, 45)); 
        btn.setBorder(BorderFactory.createEmptyBorder()); 
    }

    private void actualizarEstadoUI(String mensaje, Color color, boolean enAtencion) {
        this.estaAtendiendo = enAtencion;
        lblStatus.setText(mensaje);
        lblStatus.setForeground(color);
        lblIndicadorConexion.setForeground(color);
        
        if (enAtencion) {
            lblTicket.setForeground(AZUL_CELESTE); 
            lblTiempoAtencion.setForeground(NARANJA_TIMER); 
            btnLlamar.setBackground(new Color(50, 50, 60)); 
            btnLlamar.setForeground(Color.GRAY);
            btnLlamar.setEnabled(false);
            btnFinalizar.setBackground(VERDE_EXITO); 
            btnFinalizar.setForeground(Color.BLACK);
            btnFinalizar.setEnabled(true);
        } else {
            lblTicket.setForeground(Color.WHITE); 
            lblTiempoAtencion.setForeground(new Color(50, 50, 50)); 
            
            if (clientesEnCola > 0) {
                btnLlamar.setBackground(AZUL_NEON); 
                btnLlamar.setForeground(Color.BLACK);
                btnLlamar.setEnabled(true);
            } else {
                btnLlamar.setBackground(new Color(50, 50, 60)); 
                btnLlamar.setForeground(Color.GRAY);
                btnLlamar.setEnabled(false);
            }
            
            btnFinalizar.setBackground(new Color(50, 50, 60)); 
            btnFinalizar.setForeground(Color.GRAY);
            btnFinalizar.setEnabled(false);
        }
    }

    private void solicitarTicketConcurrente() {
        Thread hilo = new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> btnLlamar.setEnabled(false));
                
                Socket s = new Socket(config.getIpServidor(), config.getPuerto());
                s.setSoTimeout(5000);
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                out.writeObject("SOLICITAR_ESPECIAL");
                out.flush();

                Object respuesta = in.readObject();
                s.close();

                if (respuesta instanceof TicketViaje) {
                    ticketActual = (TicketViaje) respuesta;
                    horaInicioAtencion = LocalDateTime.now();
                    
                    SwingUtilities.invokeLater(() -> {
                        lblTicket.setText(ticketActual.getNumeroTicket());
                        lblDpi.setText(ticketActual.getDpi());
                        actualizarEstadoUI("EN ATENCION ESPECIAL", AZUL_NEON, true);
                        txtNombre.setEnabled(true);
                        txtApellido.setEnabled(true);
                        cmbMotivo.setEnabled(true);
                        txtComentarios.setEnabled(true);
                        txtComentarios.setText(""); 
                        segundosTranscurridos = 0;
                        lblTiempoAtencion.setText("⏱ 00:00");
                        cronometro.start();
                        txtNombre.requestFocus();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        actualizarEstadoUI("SISTEMA LIBRE", VERDE_EXITO, false);
                        JOptionPane.showMessageDialog(this, respuesta.toString(), "AVISO", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    actualizarEstadoUI("ERROR DE RED", ROJO_ALERTA, false);
                    if (clientesEnCola > 0) btnLlamar.setEnabled(true);
                });
            }
        });
        hilo.start();
    }

    private void finalizarAtencionConcurrente() {
        if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Llene nombre y apellido.", "VALIDACION FALLIDA", JOptionPane.WARNING_MESSAGE); return;
        }
        if (cmbMotivo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un motivo.", "VALIDACION FALLIDA", JOptionPane.WARNING_MESSAGE); return;
        }
        Thread hilo = new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    btnFinalizar.setEnabled(false);
                    actualizarEstadoUI("ENVIANDO DATOS AL SERVIDOR...", new Color(255, 170, 0), true);
                });
                cronometro.stop();

                LocalDateTime horaFinAtencion = LocalDateTime.now();
                long espera = Duration.between(ticketActual.getHoraIngreso(), horaInicioAtencion).getSeconds();
                long atencion = Duration.between(horaInicioAtencion, horaFinAtencion).getSeconds();
                if (atencion == 0) atencion = 1;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                String textoComentarios = txtComentarios.getText().trim();
                if (textoComentarios.isEmpty()) {
                    textoComentarios = "Sin notas adicionales";
                } else {
                    textoComentarios = textoComentarios.replace(",", ";").replace("\n", " "); 
                }
                
                String log = String.format("%s,%s,%s,%s,%s,%d,%d,%s,%s",
                        horaFinAtencion.format(formatter), ticketActual.getDpi(), txtNombre.getText(), 
                        txtApellido.getText(), cmbMotivo.getSelectedItem().toString(), atencion, (espera + atencion), 
                        txtAgente.getText(), textoComentarios);

                Socket socketFinalizar = new Socket(config.getIpServidor(), config.getPuerto());
                ObjectOutputStream outFinalizar = new ObjectOutputStream(socketFinalizar.getOutputStream());
                outFinalizar.flush();
                ObjectInputStream inFinalizar = new ObjectInputStream(socketFinalizar.getInputStream());

                outFinalizar.writeObject("FINALIZAR_ATENCION");
                outFinalizar.writeObject(log);
                outFinalizar.flush(); 
                
                String respuesta = (String) inFinalizar.readObject();
                socketFinalizar.close();

                SwingUtilities.invokeLater(() -> {
                    lblTicket.setText("---");
                    lblDpi.setText("---");
                    txtNombre.setText("");
                    txtApellido.setText("");
                    cmbMotivo.setSelectedIndex(0); 
                    txtComentarios.setText(""); 
                    txtNombre.setEnabled(false);
                    txtApellido.setEnabled(false);
                    cmbMotivo.setEnabled(false);
                    txtComentarios.setEnabled(false); 
                    actualizarEstadoUI("LISTO PARA EL SIGUIENTE", VERDE_EXITO, false);
                    lblTiempoAtencion.setText("⏱ 00:00"); 
                    lblTiempoAtencion.setForeground(new Color(50, 50, 50));
                    JOptionPane.showMessageDialog(this, "Registro guardado.\n" + respuesta, "EXITO", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    actualizarEstadoUI("FALLO DE ESCRITURA", ROJO_ALERTA, true);
                    btnFinalizar.setEnabled(true);
                    cronometro.start(); 
                });
            }
        });
        hilo.start();
    }

    private void iniciarRadarCola() {
        radarCola = new Timer(3000, e -> {
            new Thread(() -> {
                try {
                    Socket s = new Socket(config.getIpServidor(), config.getPuerto());
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                    out.writeObject("TAMANO_COLA_ESPECIAL");
                    out.flush();
                    int cantidad = (int) in.readObject();
                    s.close();
                    SwingUtilities.invokeLater(() -> {
                        clientesEnCola = cantidad;
                        if (cantidad > 0) {
                            lblEnCola.setText("COLA: " + cantidad + " CLIENTE(S) ESPECIAL");
                            lblEnCola.setForeground(AZUL_NEON); 
                            if (!estaAtendiendo) {
                                btnLlamar.setBackground(AZUL_NEON); 
                                btnLlamar.setForeground(Color.BLACK);
                                btnLlamar.setEnabled(true);
                            }
                        } else {
                            lblEnCola.setText("COLA: COLA VACIA");
                            lblEnCola.setForeground(COLOR_MUTED); 
                            if (!estaAtendiendo) {
                                btnLlamar.setBackground(new Color(50, 50, 60)); 
                                btnLlamar.setForeground(Color.GRAY);
                                btnLlamar.setEnabled(false);
                            }
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        clientesEnCola = 0;
                        lblEnCola.setText("COLA: SIN CONEXION");
                        lblEnCola.setForeground(ROJO_ALERTA);
                        if (!estaAtendiendo) btnLlamar.setEnabled(false);
                    });
                }
            }).start();
        });
        radarCola.start();
    }

    private void conectarAlDashboard() {
        new Thread(() -> {
            try {
                socketPrincipal = new Socket(config.getIpServidor(), config.getPuerto());
                outPrincipal = new ObjectOutputStream(socketPrincipal.getOutputStream());
                outPrincipal.flush(); 
                inPrincipal = new ObjectInputStream(socketPrincipal.getInputStream());
                SwingUtilities.invokeLater(() -> actualizarEstadoUI("SISTEMA CONECTADO", VERDE_EXITO, false));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> actualizarEstadoUI("ERROR DE CONEXION", ROJO_ALERTA, false));
            }
        }).start();
    }

    public static void main(String[] args) {
        agencia.configuracion.TemaAgencia.aplicar();
        SwingUtilities.invokeLater(() -> new AgenteEspecialUI().setVisible(true));
    }
}