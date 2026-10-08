class User {
  constructor({ id, name, email, credits = 50, totalGenerations = 0, isAdmin = false }) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.credits = credits;
    this.totalGenerations = totalGenerations;
    this.isAdmin = isAdmin;
    this.createdAt = new Date().toISOString();
  }
}

module.exports = User;
