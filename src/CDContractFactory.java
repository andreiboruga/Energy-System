public final class CDContractFactory implements ContractFactory {

    @Override
    public void createContract(Distributor distributor, int consumerId) {

        Contract contract = new Contract();
        contract.setPrice(distributor.getContractPrice());
        contract.setConsumerId(consumerId);
        contract.setRemainedContractMonths(distributor.getContractLength());
        distributor.setClientsNumber(distributor.getClientsNumber() + 1);
        distributor.getContracts().add(contract);
    }

}
