package org.traccar.model;

import org.traccar.api.enuns.StatusBipe;
import org.traccar.storage.StorageName;

import java.util.Date;

@StorageName("tc_bipes")
public class Bipe extends ExtendedModel {


    private String nome;
    private int time;
    private int notificacoes;
    private Date expiracao;
    private boolean ativo;
    private StatusBipe status;
    private long userDestino;
    private long userOrigem;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getNotificacoes() {
        return notificacoes;
    }

    public void incrementNotificacoes() {
            notificacoes++;
    }

    public void setNotificacoes(int notificacoes) {
        this.notificacoes = notificacoes;
    }

    public Date getExpiracao() {
        return expiracao;
    }

    public void setExpiracao(Date expiracao) {
        this.expiracao = expiracao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public StatusBipe getStatus() {
        return status;
    }

    public void setStatus(StatusBipe status) {
        this.status = status;
    }

    public long getUserDestino() {
        return userDestino;
    }

    public void setUserDestino(long userDestino) {
        this.userDestino = userDestino;
    }

    public long getUserOrigem() {
        return userOrigem;
    }

    public void setUserOrigem(long userOrigem) {
        this.userOrigem = userOrigem;
    }
}
