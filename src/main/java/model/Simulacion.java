package model;

import java.util.Random;
import java.util.function.BiConsumer;

public class Simulacion {
    private Poblacion poblacion;
    private int[][] platoBacterias;
    private int[][] platoComida;
    private int[][][] resultadosBacterias; // Matriz tridimensional para bacterias
    private int[][][] resultadosComida; // Matriz tridimensional para comida
    private Random random = new Random();

    public Simulacion(Poblacion poblacion) {
        this.poblacion = poblacion;
        this.platoBacterias = new int[20][20];
        this.platoComida = new int[20][20];
        int days = poblacion.getFechaInicio().until(poblacion.getFechaFin()).getDays() + 1;
        this.resultadosBacterias = new int[days][20][20];
        this.resultadosComida = new int[days][20][20];
        inicializarPlato();
    }

    private void inicializarPlato() {
        int centro = 10;
        int tamanoSubcuadro = 4;
        int bacteriasPorCelda = Math.max(1, poblacion.getNumBacterias() / (tamanoSubcuadro * tamanoSubcuadro)); // Asegurar al menos 1 bacteria por celda

        for (int i = centro - 2; i < centro + 2; i++) {
            for (int j = centro - 2; j < centro + 2; j++) {
                this.platoBacterias[i][j] = bacteriasPorCelda;
            }
        }

        int comidaInicial = poblacion.getComidaInicial() / (20 * 20);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                this.platoComida[i][j] = comidaInicial;
            }
        }
    }

    public void simularDia() {
        int[][] nuevoPlatoBacterias = new int[20][20];
        int[][] nuevoPlatoComida = new int[20][20];

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                int bacterias = platoBacterias[i][j];
                int comida = platoComida[i][j];
                for (int k = 0; k < bacterias; k++) {
                    simularBacteria(i, j, nuevoPlatoBacterias, nuevoPlatoComida, comida);
                }
            }
        }

        platoBacterias = nuevoPlatoBacterias;
        platoComida = nuevoPlatoComida;
        repartirComida(poblacion.getComidaFinal());
    }

    private void simularBacteria(int x, int y, int[][] nuevoPlatoBacterias, int[][] nuevoPlatoComida, int comidaEnCelda) {
        int comidaConsumida = 0;
        for (int step = 0; step < 10; step++) {
            if (comidaEnCelda >= 100) {
                comidaEnCelda -= 20;
                comidaConsumida += 20;
                int fate = random.nextInt(100);
                if (fate < 3) return; // Muere
                else if (fate >= 60 && fate < 100) moverBacteria(x, y, fate, nuevoPlatoBacterias);
            } else if (comidaEnCelda >= 10) {
                comidaEnCelda -= 10;
                comidaConsumida += 10;
                int fate = random.nextInt(100);
                if (fate < 6) return; // Muere
                else if (fate >= 20 && fate < 100) moverBacteria(x, y, fate, nuevoPlatoBacterias);
            } else {
                int fate = random.nextInt(100);
                if (fate < 20) return; // Muere
                else if (fate >= 60 && fate < 100) moverBacteria(x, y, fate, nuevoPlatoBacterias);
            }
        }
        nuevoPlatoBacterias[x][y]++;
        reproducirBacterias(x, y, nuevoPlatoBacterias, comidaConsumida);
    }

    private void moverBacteria(int x, int y, int fate, int[][] nuevoPlatoBacterias) {
        int newX = x, newY = y;
        if (fate >= 60 && fate < 65) newX = x - 1;
        else if (fate >= 65 && fate < 70) newX = x + 1;
        else if (fate >= 70 && fate < 75) newY = y - 1;
        else if (fate >= 75 && fate < 80) newY = y + 1;

        if (newX >= 0 && newX < 20 && newY >= 0 && newY < 20) {
            nuevoPlatoBacterias[newX][newY]++;
        } else {
            nuevoPlatoBacterias[x][y]++;
        }
    }

    private void reproducirBacterias(int x, int y, int[][] nuevoPlatoBacterias, int comidaConsumida) {
        if (comidaConsumida >= 150) {
            nuevoPlatoBacterias[x][y] += 3;
        } else if (comidaConsumida >= 100) {
            nuevoPlatoBacterias[x][y] += 2;
        } else if (comidaConsumida >= 50) {
            nuevoPlatoBacterias[x][y] += 1;
        }
    }

    private void repartirComida(int comidaTotal) {
        int comidaPorCelda = comidaTotal / (20 * 20);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                platoComida[i][j] += comidaPorCelda;
            }
        }
    }

    private void guardarResultadoDia(int day) {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                resultadosBacterias[day][i][j] = platoBacterias[i][j];
                resultadosComida[day][i][j] = platoComida[i][j];
            }
        }
    }

    public int[][][] getResultadosBacterias() {
        return resultadosBacterias;
    }

    public int[][][] getResultadosComida() {
        return resultadosComida;
    }

    public int[][] getPlatoBacterias() {
        return platoBacterias;
    }
}
























