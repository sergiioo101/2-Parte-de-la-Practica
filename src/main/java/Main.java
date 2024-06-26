import data.FileManager;
import model.Experimento;
import model.Poblacion;
import model.Simulacion;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Experimento currentExperiment;
    private static JFrame frame;
    private static JList<String> listPoblaciones; // Lista para mostrar nombres de poblaciones
    private static DefaultListModel<String> listModel; // Modelo de datos para la lista
    private static JPanel simulationGridPanel;
    private static final String[] LUMINOSIDAD_OPTIONS = {"Alta", "Media", "Baja"};
    private static JLabel dayLabel; // Nuevo JLabel para mostrar el día actual
    private static boolean runningSimulation; // Variable para controlar la ejecución de la simulación
    private static Thread simulacionThread; // Variable para el hilo de la simulación


    public static void main(String[] args) {
        // Mensaje de bienvenida
        JOptionPane.showMessageDialog(null, "¡Bienvenido al Gestor de Experimentos de Cultivo de Bacterias!", "Bienvenida", JOptionPane.INFORMATION_MESSAGE);
        SwingUtilities.invokeLater(Main::createAndShowGUI);
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

    }

    private static void createAndShowGUI() {
        frame = new JFrame("Gestión de Experimentos de Cultivo de Bacterias");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(173, 216, 230)); // Azul claro
        tabbedPane.addTab("Inicio", createWelcomePanel());
        tabbedPane.addTab("Experimentos", createExperimentPanel());
        tabbedPane.addTab("Poblaciones", createPopulationPanel());
        tabbedPane.addTab("Detalles", createDetailsPanel());
        tabbedPane.addTab("Simulación", createSimulationPanel());

        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Texto grande
        JLabel welcomeLabel = new JLabel("Gestor de Experimentos de Cultivo de Bacterias", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // Imagen de bacteria
        String imagePath = "C:\\Users\\34620\\Downloads\\pruebaprogra.jpg"; // Asegúrate de mover la imagen a este directorio
        ImageIcon bacteriaIcon = new ImageIcon(imagePath);

        if (bacteriaIcon.getIconWidth() == -1) {
            JLabel errorLabel = new JLabel("Error al cargar la imagen desde: " + imagePath);
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        } else {
            // Redimensionar la imagen si es necesario
            Image bacteriaImage = bacteriaIcon.getImage();
            Image scaledImage = bacteriaImage.getScaledInstance(400, 400, Image.SCALE_SMOOTH); // Ajusta el tamaño según tus necesidades
            bacteriaIcon = new ImageIcon(scaledImage);

            JLabel imageLabel = new JLabel(bacteriaIcon);
            panel.add(imageLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    private static JPanel createExperimentPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnLoad = new JButton("Cargar Experimento");
        JButton btnSave = new JButton("Guardar Experimento");
        JButton btnNew = new JButton("Nuevo Experimento");

        btnLoad.addActionListener(e -> loadExperiment());
        btnSave.addActionListener(e -> saveExperiment());
        btnNew.addActionListener(e -> createNewExperiment());

        panel.add(btnLoad);
        panel.add(btnSave);
        panel.add(btnNew);

        return panel;
    }

    private static void loadExperiment() {
        JFileChooser fileChooser = new JFileChooser(FileManager.DIRECTORY);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getName();
            currentExperiment = FileManager.loadExperiment(filename);
            if (currentExperiment != null) {
                JOptionPane.showMessageDialog(frame, "Experimento cargado correctamente.", "Cargar", JOptionPane.INFORMATION_MESSAGE);
                updatePoblacionesList();
            } else {
                JOptionPane.showMessageDialog(frame, "Error al cargar el experimento.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void saveExperiment() {
        if (currentExperiment == null) {
            JOptionPane.showMessageDialog(frame, "No hay experimento activo para guardar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String filename = JOptionPane.showInputDialog(frame, "Ingrese el nombre del archivo para guardar el experimento:");
        if (filename != null && !filename.isEmpty()) {
            currentExperiment.setArchivo(filename);
            FileManager.saveExperiment(currentExperiment, filename);
            JOptionPane.showMessageDialog(frame, "Experimento guardado correctamente.", "Guardar", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void createNewExperiment() {
        String filename = JOptionPane.showInputDialog(frame, "Ingrese el nombre del archivo para el nuevo experimento:");
        if (filename != null && !filename.isEmpty()) {
            currentExperiment = new Experimento(filename);
            JOptionPane.showMessageDialog(frame, "Nuevo experimento creado.", "Nuevo Experimento", JOptionPane.INFORMATION_MESSAGE);
            updatePoblacionesList();
        }
    }

    private static JPanel createPopulationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<>();
        listPoblaciones = new JList<>(listModel);
        listPoblaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(listPoblaciones);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        JButton btnAdd = new JButton("Añadir Población");
        JButton btnEdit = new JButton("Editar Población");
        JButton btnRemove = new JButton("Eliminar Población");

        btnAdd.addActionListener(e -> addPopulation());
        btnEdit.addActionListener(e -> editPopulation());
        btnRemove.addActionListener(e -> removePopulation());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(btnRemove);

        // Añadir JComboBox y botón para ordenación
        String[] sortOptions = {"Nombre", "Fecha de Inicio", "Número de Bacterias"};
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        JButton btnSort = new JButton("Ordenar");

        btnSort.addActionListener(e -> {
            String selectedOption = (String) sortComboBox.getSelectedItem();
            if (selectedOption != null) {
                switch (selectedOption) {
                    case "Nombre":
                        currentExperiment.ordenarPoblacionesPorNombre();
                        break;
                    case "Fecha de Inicio":
                        currentExperiment.ordenarPoblacionesPorFechaInicio();
                        break;
                    case "Número de Bacterias":
                        currentExperiment.ordenarPoblacionesPorNumeroBacterias();
                        break;
                }
                updatePoblacionesList();
            }
        });

        buttonsPanel.add(sortComboBox);
        buttonsPanel.add(btnSort);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static void addPopulation() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField();
        JTextField numDaysField = new JTextField();
        JTextField numBacteriasField = new JTextField();
        JTextField temperaturaField = new JTextField();
        JComboBox<String> luminosidadComboBox = new JComboBox<>(LUMINOSIDAD_OPTIONS);
        JTextField comidaInicialField = new JTextField();
        JTextField comidaFinalField = new JTextField();
        JComboBox<String> tipoAlimentacionComboBox = new JComboBox<>(new String[]{"Lineal", "Constante", "Alternativa"});
        JTextField diaIncrementoField = new JTextField();
        JTextField comidaMaximaField = new JTextField();

        panel.add(new JLabel("Nombre:"));
        panel.add(nameField);
        panel.add(new JLabel("Número de Días (variable):"));
        panel.add(numDaysField);
        panel.add(new JLabel("Número de Bacterias:"));
        panel.add(numBacteriasField);
        panel.add(new JLabel("Temperatura:"));
        panel.add(temperaturaField);
        panel.add(new JLabel("Luminosidad:"));
        panel.add(luminosidadComboBox);
        panel.add(new JLabel("Comida Inicial (en microgramos):"));
        panel.add(comidaInicialField);
        panel.add(new JLabel("Comida Final (en microgramos):"));
        panel.add(comidaFinalField);
        panel.add(new JLabel("Tipo de Alimentación:"));
        panel.add(tipoAlimentacionComboBox);
        panel.add(new JLabel("Día de Incremento:"));
        panel.add(diaIncrementoField);
        panel.add(new JLabel("Comida Máxima:"));
        panel.add(comidaMaximaField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Añadir Población", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nameField.getText();
                LocalDate fechaInicio = LocalDate.now();
                int numDias = Integer.parseInt(numDaysField.getText());
                LocalDate fechaFin = fechaInicio.plusDays(numDias);
                int numBacterias = Integer.parseInt(numBacteriasField.getText());
                double temperatura = Double.parseDouble(temperaturaField.getText());
                String luminosidad = luminosidadComboBox.getSelectedItem().toString();
                int comidaInicial = Integer.parseInt(comidaInicialField.getText());
                int comidaFinal = Integer.parseInt(comidaFinalField.getText());
                String tipoAlimentacion = tipoAlimentacionComboBox.getSelectedItem().toString();
                int diaIncremento = Integer.parseInt(diaIncrementoField.getText());
                int comidaMaxima = Integer.parseInt(comidaMaximaField.getText());

                Poblacion nuevaPoblacion = new Poblacion(nombre, fechaInicio, fechaFin, numBacterias, temperatura, luminosidad, comidaInicial, comidaFinal, diaIncremento, comidaMaxima, tipoAlimentacion);
                currentExperiment.addPoblacion(nuevaPoblacion);
                updatePoblacionesList();
                JOptionPane.showMessageDialog(frame, "Población añadida correctamente.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Por favor, introduzca números válidos en los campos numéricos.", "Error de Número", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void editPopulation() {
        String selected = listPoblaciones.getSelectedValue();
        if (selected != null) {
            Poblacion poblacion = currentExperiment.getPoblacion(selected);

            JPanel panel = new JPanel(new GridLayout(0, 2));
            JTextField nameField = new JTextField(poblacion.getNombre());
            JTextField numDaysField = new JTextField(String.valueOf(poblacion.getFechaInicio().until(poblacion.getFechaFin()).getDays()));
            JTextField numBacteriasField = new JTextField(String.valueOf(poblacion.getNumBacterias()));
            JTextField temperaturaField = new JTextField(String.valueOf(poblacion.getTemperatura()));
            JComboBox<String> luminosidadComboBox = new JComboBox<>(LUMINOSIDAD_OPTIONS);
            luminosidadComboBox.setSelectedItem(poblacion.getLuminosidad());
            JTextField comidaInicialField = new JTextField(String.valueOf(poblacion.getComidaInicial()));
            JTextField comidaFinalField = new JTextField(String.valueOf(poblacion.getComidaFinal()));
            JComboBox<String> tipoAlimentacionComboBox = new JComboBox<>(new String[]{"linear", "constant", "alternating"});
            tipoAlimentacionComboBox.setSelectedItem(poblacion.getTipoAlimentacion());
            JTextField diaIncrementoField = new JTextField(String.valueOf(poblacion.getDiaIncremento()));
            JTextField comidaMaximaField = new JTextField(String.valueOf(poblacion.getComidaMaxima()));

            panel.add(new JLabel("Nombre:"));
            panel.add(nameField);
            panel.add(new JLabel("Número de Días (variable):"));
            panel.add(numDaysField);
            panel.add(new JLabel("Número de Bacterias:"));
            panel.add(numBacteriasField);
            panel.add(new JLabel("Temperatura:"));
            panel.add(temperaturaField);
            panel.add(new JLabel("Luminosidad:"));
            panel.add(luminosidadComboBox);
            panel.add(new JLabel("Comida Inicial (en microgramos):"));
            panel.add(comidaInicialField);
            panel.add(new JLabel("Comida Final (en microgramos):"));
            panel.add(comidaFinalField);
            panel.add(new JLabel("Tipo de Alimentación:"));
            panel.add(tipoAlimentacionComboBox);
            panel.add(new JLabel("Día de Incremento:"));
            panel.add(diaIncrementoField);
            panel.add(new JLabel("Comida Máxima:"));
            panel.add(comidaMaximaField);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Editar Población", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String nombre = nameField.getText();
                    LocalDate fechaInicio = poblacion.getFechaInicio();
                    int numDias = Integer.parseInt(numDaysField.getText());
                    LocalDate fechaFin = fechaInicio.plusDays(numDias);
                    int numBacterias = Integer.parseInt(numBacteriasField.getText());
                    double temperatura = Double.parseDouble(temperaturaField.getText());
                    String luminosidad = luminosidadComboBox.getSelectedItem().toString();
                    int comidaInicial = Integer.parseInt(comidaInicialField.getText());
                    int comidaFinal = Integer.parseInt(comidaFinalField.getText());
                    String tipoAlimentacion = tipoAlimentacionComboBox.getSelectedItem().toString();
                    int diaIncremento = Integer.parseInt(diaIncrementoField.getText());
                    int comidaMaxima = Integer.parseInt(comidaMaximaField.getText());

                    poblacion.setNombre(nombre);
                    poblacion.setFechaFin(fechaFin);
                    poblacion.setNumBacterias(numBacterias);
                    poblacion.setTemperatura(temperatura);
                    poblacion.setLuminosidad(luminosidad);
                    poblacion.setComidaInicial(comidaInicial);
                    poblacion.setComidaFinal(comidaFinal);
                    poblacion.setDiaIncremento(diaIncremento);
                    poblacion.setComidaMaxima(comidaMaxima);
                    poblacion.setTipoAlimentacion(tipoAlimentacion);
                    poblacion.generarPlanAlimentacion();

                    updatePoblacionesList();
                    JOptionPane.showMessageDialog(frame, "Población editada correctamente.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Por favor, introduzca números válidos en los campos numéricos.", "Error de Número", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Seleccione una población para editar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void removePopulation() {
        String selected = listPoblaciones.getSelectedValue();
        if (selected != null) {
            currentExperiment.removePoblacion(selected);
            updatePoblacionesList();
            JOptionPane.showMessageDialog(frame, "Población eliminada correctamente.");
        } else {
            JOptionPane.showMessageDialog(frame, "Seleccione una población para eliminar.");
        }
    }

    private static void updatePoblacionesList() {
        listModel.clear();
        if (currentExperiment != null && currentExperiment.getPoblaciones() != null) {
            for (Poblacion p : currentExperiment.getPoblaciones()) {
                listModel.addElement(p.getNombre());
            }
        }
    }

    private static JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(detailsArea);

        JButton btnShowDetails = new JButton("Mostrar Detalles");

        btnShowDetails.addActionListener(e -> {
            Poblacion selectedPoblacion = currentExperiment.getPoblacion(listPoblaciones.getSelectedValue());
            if (selectedPoblacion != null) {
                detailsArea.setText(getPopulationDetails(selectedPoblacion));
            } else {
                detailsArea.setText("Seleccione una población para ver detalles.");
            }
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(btnShowDetails);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static JPanel createSimulationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Crear el panel de la cuadrícula de simulación
        simulationGridPanel = new JPanel(new GridLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                for (int i = 0; i < getWidth(); i += getWidth() / 20) {
                    for (int j = 0; j < getHeight(); j += getHeight() / 20) {
                        g2.drawRect(i, j, getWidth() / 20, getHeight() / 20);
                    }
                }
            }
        };

        panel.add(simulationGridPanel, BorderLayout.CENTER);

        // Crear el JLabel para mostrar el día actual
        dayLabel = new JLabel("Día: 0");
        panel.add(dayLabel, BorderLayout.NORTH);

        // Botón para ejecutar la simulación
        JButton btnRunSimulation = new JButton("Ejecutar Simulación");
        btnRunSimulation.addActionListener(e -> runSimulation());

        // Botón para detener la simulación
        JButton btnStopSimulation = new JButton("Detener Simulación");
        btnStopSimulation.addActionListener(e -> runningSimulation = false);

        // Botón para reiniciar la simulación
        JButton btnRestartSimulation = new JButton("Reiniciar Simulación");
        btnRestartSimulation.addActionListener(e -> {
            runningSimulation = false;
            if (simulacionThread != null && simulacionThread.isAlive()) {
                simulacionThread.interrupt();
            }
            simulationGridPanel.removeAll();
            simulationGridPanel.revalidate();
            simulationGridPanel.repaint();
            dayLabel.setText("Día: 0");
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(btnRunSimulation);
        buttonsPanel.add(btnStopSimulation);
        buttonsPanel.add(btnRestartSimulation);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static void runSimulation() {
        Poblacion selectedPoblacion = currentExperiment.getPoblacion(listPoblaciones.getSelectedValue());
        if (selectedPoblacion != null) {
            simulationGridPanel.removeAll();
            simulationGridPanel.revalidate();
            simulationGridPanel.repaint();
            runningSimulation = true;

            simulacionThread = new Thread(() -> {
                Simulacion simulacion = new Simulacion(selectedPoblacion);
                int duracion = selectedPoblacion.getFechaInicio().until(selectedPoblacion.getFechaFin()).getDays() + 1;
                for (int day = 0; day < duracion; day++) {
                    if (!runningSimulation) break;
                    simulacion.simularDia(day); // Pasar el día actual
                    int[][] plato = simulacion.getPlatoBacterias();
                    int finalDay = day;
                    SwingUtilities.invokeLater(() -> {
                        dayLabel.setText("Día: " + (finalDay + 1)); // Actualizar el JLabel del día
                        actualizarCuadricula(plato);
                        simulationGridPanel.revalidate();
                        simulationGridPanel.repaint();
                    });
                    try {
                        Thread.sleep(1000); // Pausa de 1 segundo para ver los cambios día a día
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            simulacionThread.start();
        } else {
            JOptionPane.showMessageDialog(frame, "Seleccione una población para simular.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static void actualizarCuadricula(int[][] plato) {
        simulationGridPanel.removeAll();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                JLabel cell = new JLabel();
                cell.setOpaque(true);
                int bacterias = plato[i][j];
                if (bacterias >= 20) {
                    cell.setBackground(Color.RED);
                } else if (bacterias >= 15) {
                    cell.setBackground(Color.MAGENTA);
                } else if (bacterias >= 10) {
                    cell.setBackground(Color.ORANGE);
                } else if (bacterias >= 5) {
                    cell.setBackground(Color.YELLOW);
                } else if (bacterias >= 1) {
                    cell.setBackground(Color.GREEN);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                simulationGridPanel.add(cell);
            }
        }
        simulationGridPanel.revalidate();
        simulationGridPanel.repaint();
    }

    private static String getPopulationDetails(Poblacion poblacion) {
        StringBuilder details = new StringBuilder();
        details.append("Detalles de la Población:\n");
        details.append("Nombre: ").append(poblacion.getNombre()).append("\n");
        details.append("Fecha de Inicio: ").append(poblacion.getFechaInicio()).append("\n");
        details.append("Fecha de Fin: ").append(poblacion.getFechaFin()).append("\n");
        details.append("Número de Bacterias: ").append(poblacion.getNumBacterias()).append("\n");
        details.append("Temperatura: ").append(poblacion.getTemperatura()).append("\n");
        details.append("Luminosidad: ").append(poblacion.getLuminosidad()).append("\n");
        details.append("Comida Inicial: ").append(poblacion.getComidaInicial()).append("\n");
        details.append("Día de Incremento Máximo: ").append(poblacion.getDiaIncremento()).append("\n");
        details.append("Comida Máxima en el Día de Incremento: ").append(poblacion.getComidaMaxima()).append("\n");
        details.append("Comida Final en Día 30: ").append(poblacion.getComidaFinal()).append("\n");
        details.append("\n");

        details.append("Cálculo estimado de la cantidad de comida por día:\n");
        List<Integer> comidaPorDia = calcularComidaPorDia(poblacion);
        for (int i = 0; i < comidaPorDia.size(); i++) {
            details.append("Día ").append(i + 1).append(": ").append(comidaPorDia.get(i)).append(" unidades\n");
        }
        return details.toString();
    }

    private static List<Integer> calcularComidaPorDia(Poblacion poblacion) {
        List<Integer> comidaPorDia = new ArrayList<>();
        int comidaInicial = poblacion.getComidaInicial();
        int diaIncremento = poblacion.getDiaIncremento();
        int comidaMaxima = poblacion.getComidaMaxima();
        int comidaFinal = poblacion.getComidaFinal();
        int numDias = poblacion.getFechaInicio().until(poblacion.getFechaFin()).getDays() + 1;

        for (int dia = 1; dia <= numDias; dia++) {
            int comidaDia;
            if (dia <= diaIncremento) {
                double incrementoDiario = (double) (comidaMaxima - comidaInicial) / diaIncremento;
                comidaDia = (int) (comidaInicial + (dia - 1) * incrementoDiario);
            } else {
                double decrementoDiario = (double) (comidaMaxima - comidaFinal) / (numDias - diaIncremento);
                comidaDia = (int) (comidaMaxima - (dia - diaIncremento) * decrementoDiario);
            }
            comidaPorDia.add(comidaDia);
        }
        return comidaPorDia;
    }

    private static void mostrarResultadosFinales(int[][][] resultadosBacterias, int[][][] resultadosComida) {
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        StringBuilder results = new StringBuilder();

        for (int day = 0; day < resultadosBacterias.length; day++) {
            results.append("Día ").append(day + 1).append(":\nBacterias:\n");
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    results.append(String.format("%3d", resultadosBacterias[day][i][j])).append(" ");
                }
                results.append("\n");
            }
            results.append("Comida:\n");
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    results.append(String.format("%3d", resultadosComida[day][i][j])).append(" ");
                }
                results.append("\n");
            }
            results.append("\n");
        }

        JOptionPane.showMessageDialog(frame, new JScrollPane(resultsArea), "Resultados de la Simulación", JOptionPane.INFORMATION_MESSAGE);
    }
}
































