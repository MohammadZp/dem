package ir.dotin.exam.di;

public record CardService(CardRepository cardRepository) {

    public void save(Card card) {
        cardRepository.save(card);
    }
}
