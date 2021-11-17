import java.util.List;

public interface Strategy {

    int chooseProducers(List<Producer> producers, int energyNeeded,
                        Distributor distributor); //Alege producatorii si returneaza costul productiei

}
