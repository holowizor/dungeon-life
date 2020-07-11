package dungeonlife


open class MonsterState(var health: Float = 20f,
                        val killReward: Float = 5f,
                        val attack: Float = 3f) {

    fun isAlive() = health > 0f

    fun decreaseHealth(amount: Float) {
        health -= amount;
        if (health < 0f) health = 0f;
    }
}

class HeroState(health: Float = 100f,
                attack: Float = 10f) : MonsterState(health, attack) {
    var exp = 0f
    fun increaseExp(amount: Float) {
        exp += amount
    }
}
