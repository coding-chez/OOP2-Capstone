<div align="center">
  
 ![header(1)](https://github.com/user-attachments/assets/4dd0eebd-3bd3-4e09-a710-bdb52f95d179)

</div>

<div align="center">
  
# OOP2 - Capstone Project

</div>

<div align="center">
  
<h3>
Team Members:
</h3>

<p>
Chestine May Mari C. Cabiso |
Axille Dayonot |
Ruhmer Jairus R. Espina |
Lovely Shane P. Ong
</p>

</div>

---


## About the Project 💜✨

**TypeWiz** is an action-packed typing game where you play as a wizard defending your tower from waves of flying mobs. Each enemy is tied to a word—type it correctly to cast spells and destroy them before they reach your tower. Use the following controls to enhance your experience:
- **Spacebar**: Attack monsters.
- **Shift**: Switch targets.
- **Enter**: Restart the game after losing.

Stay alert—if monsters breach your defenses, your health will drop. Lose all your health, and it’s game over. Press Enter to try again and sharpen your typing skills.

<img src="https://github.com/user-attachments/assets/473d53ad-e2f5-4f8d-8aea-6b65eaf959f5" width="150" height="130" align="left" />

### Difficulty Levels:
- 💫 **APPRENTICE**: Fewer monsters, simpler words.
- 🧙‍♂️ **WIZARD**: Balanced difficulty for seasoned typists.
- 🔮 **ARCHMAGE**: Fast waves and tricky words to truly test your reflexes.

The **OOP2 Capstone Project**, **TypeWiz**, showcases object-oriented programming principles applied to game development. Built with FXGL, a Java game development library, it leverages modern tools to deliver an engaging and interactive experience beyond the traditional FXML framework.

Can you survive the onslaught and become the ultimate typing wizard?

---

## OOP Principles 🤓✨

Our project highlights several key OOP principles:

- **Encapsulation:** The game logic, player data, and UI components are modularized into distinct classes, ensuring data hiding and reducing interdependencies.
- **Inheritance:** We utilized inheritance to create reusable and extendable classes, such as shared behaviors for different game entities (e.g., enemies, power-ups, and the main player).
- **Polymorphism:** Polymorphism is evident in how our game entities handle different actions and behaviors dynamically, such as varied responses to collisions or player interactions.
- **Abstraction:** Core functionalities are abstracted into base classes, allowing us to manage the complexity of the codebase effectively.

---

## Generics Usage 🧩✨

Generics are used in **TypeWiz** to improve code flexibility and type safety. They allow us to write cleaner, reusable logic for managing game entities and operations.

- **Entity Management:**  
  The `EntityManager` class uses generics (e.g., `List<Entity>`) to handle various entity types like *Gargoyles* and *Grimouges* dynamically.

- **Predicate-Based Filtering:**  
  Methods like `findEntities(Predicate<Entity> predicate)` allow filtering entities based on custom conditions using generic predicates.

- **Type-Specific Operations:**  
  `getActiveEntitiesByType(Game.EntityType type)` returns a filtered list of entities by their type, leveraging generics for safe casting.

This approach keeps the game logic modular and adaptable, making it easier to introduce new entities or behaviors without rewriting core functionality.

---

## Design Patterns Used 🎆✨

We incorporated several design patterns to ensure maintainable and efficient code:

<img src="https://github.com/user-attachments/assets/f4be2632-bdb9-410b-b1a6-00f6fe8e5ee6" width="150" height="110" align="right" />

- **Singleton Pattern:** Used for managing game states and shared resources like the game timer and score tracker.
- **Observer Pattern:** Implemented for event-driven communication between game components, such as notifying observers when the player achieves a milestone.
- **Factory Pattern:** Utilized for creating game entities dynamically without exposing the instantiation logic.

---

## Advanced Concepts Incorporated 💻✨

### Threads and Multithreading

The **TypeWiz** game utilizes threads and multithreading to ensure optimal performance and a seamless user experience:

- **Game Loop Execution:** A dedicated thread is responsible for managing the game loop, ensuring smooth animations, real-time collision detection, and consistent gameplay updates.
- **Asynchronous Background Tasks:** Resource-intensive operations, such as database interactions using JDBC or loading large assets, are executed on separate background threads. This prevents the main UI thread from freezing or becoming unresponsive.
- **Concurrent Gameplay Mechanics:** Multithreading is employed to handle concurrent gameplay mechanics, such as spawning enemies, tracking player inputs, and managing game events, ensuring the game remains responsive and immersive even under heavy computational loads.

### Serialization

Serialization is utilized in **TypeWiz** to save and retrieve game states efficiently, ensuring a seamless gaming experience. The following files were applied for serialization:

- **TypeWizApp.java**: Handles the game's overall serialization processes, saving the application state when needed.
- **Game.java**: Implements the serialization of specific game components, such as player progress and current game states.

Serialization allows the game to maintain continuity by storing and loading data, enhancing the overall user experience.

### JDBC (Java Database Connectivity)
To enhance the game's functionality, we integrated JDBC:
- **Database Management:** Player data, including scores and progression, is stored in a relational database.
- **SQL Integration:** We established secure and efficient connections to the database for CRUD (Create, Read, Update, Delete) operations.
- **Leaderboards:** A dynamic leaderboard fetches top player scores in real-time using JDBC to keep players engaged and motivated.

---

## Features
<img src="https://github.com/user-attachments/assets/8aa88937-db30-46b6-adcf-b6c71043fcd3" width="200" height="220" align="right" />

- **Real-time Typing-based Combat:** Engage in fast-paced gameplay by typing words to cast spells and defeat waves of enemies.
- **Dynamic Difficulty Levels:** Select from Apprentice (easy), Wizard (moderate), or Archmage (challenging) modes to match your skill level and improve over time.
- **Leaderboard Integration:** Compete globally by tracking high scores and showcasing your typing prowess.
- **Multithreaded Game Loop:** Enjoy smooth animations and responsive gameplay mechanics, powered by an efficient multithreaded architecture.
- **Serialization Support:** Save and retrieve game states seamlessly to maintain progress across sessions.
- **Enhanced Modular Design:** The codebase is structured with clean, maintainable modules, leveraging OOP principles and design patterns for future scalability.


---

## Getting Started 🚀

To get started with TypeWiz, follow these steps:

### Prerequisites
- Java Development Kit (JDK 8 or higher)
- A relational database (e.g., MySQL)
- An IDE or text editor (e.g., IntelliJ, Eclipse)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/coding-chez/OOP2-Capstone.git
2. Configure the database:
- Create a database and import the provided schema.
- Update the db.properties file with your database credentials.
3. Build and run the project:

       ./gradlew run

---

### From Brainstorming to Open Source 🔮✨

Our journey began with a brainstorming session to decide on the concept of our capstone project. We aimed to create a fun and educational typing game that enhances players' typing speed and accuracy.

Each team member contributed to the ideation, design, and development process:

- **Conceptualization:** We collaborated to identify the core mechanics and objectives of TypeWiz, ensuring they aligned with our goals as a team.
- **Development:** Responsibilities were divided based on individual strengths—team members worked on coding, designing, and integrating features.
- **Testing and Refinement:** We rigorously tested the game for bugs and usability, incorporating feedback to polish the final product.
- **Open Source Contribution:** To give back to the community, we made the project open source, allowing others to learn from and extend our work.

### Contributing

We welcome contributions from the community! Please follow these steps to contribute:

1. Fork the repository.
2. Create a feature branch:

        git checkout -b feature-name

3. Commit your changes:

       git commit -m "Add feature-name"

4. Push to your branch:

        git push origin feature-name

5. Open a pull request.


---

<div align="center">
  
  ![footer(1)](https://github.com/user-attachments/assets/6b683d29-6530-442c-a289-10199c004313)

  
</div>
