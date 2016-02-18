package ${package};

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author 70744416353
 */
@Configuration(resource = "app")
public class AppConfig {

    private String url;

    private Long sessionTimeout = new Long(90 * 60 * 1000);

    @Name("jwt.key")
    private String chave;

    @Name("jwt.minutes")
    private Float tempo;

    @Name("jwt.issuer")
    private String remetente;

    @Name("jwt.audience")
    private String destinatario;

    /**
     *
     */
    public AppConfig() {
        super();
    }

    /**
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @return
     */
    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     *
     * @return
     */
    public String getChave() {
        return chave;
    }

    /**
     *
     * @param chave
     */
    public void setChave(String chave) {
        this.chave = chave;
    }

    /**
     *
     * @return
     */
    public Float getTempo() {
        return tempo;
    }

    /**
     *
     * @param tempo
     */
    public void setTempo(Float tempo) {
        this.tempo = tempo;
    }

    /**
     *
     * @return
     */
    public String getRemetente() {
        return remetente;
    }

    /**
     *
     * @param remetente
     */
    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    /**
     *
     * @return
     */
    public String getDestinatario() {
        return destinatario;
    }

    /**
     *
     * @param destinatario
     */
    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

}
