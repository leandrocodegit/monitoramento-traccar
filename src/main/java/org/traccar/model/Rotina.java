package org.traccar.model;

import org.traccar.storage.StorageName;


import java.util.Date;

@StorageName("tc_rotinas")
public class Rotina extends ExtendedModel {

    private String nome;
    private String type;

    private Date horaInicial;
    private Date horaFinal;
    private long deviceId;
    private long geofenceId;
    private long userId;
    private boolean ativo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getHoraInicial() {
        return horaInicial;
    }

    public void setHoraInicial(Date horaInicial) {
        this.horaInicial = horaInicial;
    }

    public Date getHoraFinal() {
        return horaFinal;
    }

    public void setHoraFinal(Date horaFinal) {
        this.horaFinal = horaFinal;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getGeofenceId() {
        return geofenceId;
    }

    public void setGeofenceId(long geofenceId) {
        this.geofenceId = geofenceId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return "Rotina{" +
                "nome='" + nome + '\'' +
                ", type='" + type + '\'' +
                ", horaInicial=" + horaInicial +
                ", horaFinal=" + horaFinal +
                ", deviceId=" + deviceId +
                ", geofenceId=" + geofenceId +
                ", userId=" + userId +
                '}';
    }
}
