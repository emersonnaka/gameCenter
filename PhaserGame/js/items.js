class Coin extends Phaser.Sprite {
    constructor(game, x, y, asset) {
        super(game, x, y, asset)
        this.game.physics.arcade.enable(this)
        this.body.allowGravity = false
        this.autoCull = true
        this.points = 1

        this.animations.add('spin', [0, 1, 2, 3, 4, 5], 10, true)
        this.animations.play('spin')
    }
}