package br.edu.ifsul.venancio.tenisshop.model.util;

/**
 * Classifica o quão forte é uma senha em texto puro, segundo os critérios
 * avaliados por SenhaUtil.avaliarForca(). Usado tanto pelo medidor visual
 * nas telas (fraca/média/forte) quanto pela trava que exige senha forte
 * para salvar.
 *
 * @author Geovane Griesang
 */
public enum NivelForcaSenha {
    FRACA,
    MEDIA,
    FORTE
}
