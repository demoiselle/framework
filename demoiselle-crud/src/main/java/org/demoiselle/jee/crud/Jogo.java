package org.demoiselle.jee.crud;

import java.util.Stack;
import java.util.stream.Stream;

public class Jogo {

    public enum NomeCarta {
        REI("Rei"),
        DAMA("Dama"),
        VALETE("Valete"),
        DEZ("Dez"),
        NOVE("Nove"),
        OITO("Oito"),
        SETE("Sete"),
        SEIS("Seis"),
        CINCO("Cinco"),
        QUATRO("Quatro"),
        TRES("Três"),
        DOIS("Dois"),
        AS("Ás");

        private String nome;

        NomeCarta(String nome) {
            this.nome = nome;
        }

        public String getNome() {
            return nome;
        }
    }

    public static enum Naipe {
        OURO("Ouro"),
        ESPADA("Espadas"),
        PAUS("Paus"),
        COPAS("Copas");

        private String nome;

        Naipe(String nome) {
            this.nome = nome;
        }
    }

    public interface Carta {
        Naipe getNaipe();
        NomeCarta getNomeCarta();
    }

    public interface Baralho {
        Stack<Carta> getCartas();
    }

    public interface JogoBaralho {
        Baralho getBaralho();
        void embaralhar();

    }

    public static class DefaultCarta implements Carta {
        private Naipe naipe;
        private NomeCarta nomeCarta;

        public DefaultCarta(Naipe naipe, NomeCarta nomeCarta) {
            this.naipe = naipe;
            this.nomeCarta = nomeCarta;
        }

        public Naipe getNaipe() {
            return naipe;
        }

        public NomeCarta getNomeCarta() {
            return nomeCarta;
        }
    }

    public static abstract class Deck {
        private Stack<Carta> cartas = new Stack<>();

        public abstract boolean podeReceber(Carta carta);
        public abstract boolean podeRetirar(Carta carta);

    }

    public enum BaralhoPadrao implements Baralho {
        INSTANCE;

        Stack<Carta> cartas = initBaralhoPadrao();

        private Stack<Carta> initBaralhoPadrao() {
            cartas = new Stack<Carta>();
            for (Naipe naipe : Naipe.values()) {
                for (NomeCarta nomeCarta : NomeCarta.values()) {
                    cartas.push(new DefaultCarta(naipe, nomeCarta));
                }
            }
            return cartas;
        }


        @Override
        public Stack<Carta> getCartas() {
            return cartas;
        }
    }




}
