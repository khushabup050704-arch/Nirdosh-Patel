class CreditTransaction {
  constructor({ id, userId, amount, type, description }) {
    this.id = id;
    this.userId = userId;
    this.amount = amount;
    this.type = type; // generation, purchase, bonus, admin_grant
    this.description = description;
    this.timestamp = Date.now();
  }
}

module.exports = CreditTransaction;
