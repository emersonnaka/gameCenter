class Snake extends Phaser.Sprite {
    constructor(game, x, y, asset) {
        super(game, x, y, asset)
        this.game.physics.arcade.enable(this)
        this.body.allowGravity = false
        this.autoCull = true
        this.body.setSize(49, 17, 9, 18)
        this.body.immovable = true // kinematic

        this.scale.setTo(1.3, 1.3)

        this.animations.add('move', [3, 0, 1, 2], 5, true)
        this.animations.play('move')
    }

    start() {
        // correcao do problema de ancora do TILED
        this.targetY -= this.height

        let tweenA = this.game.add.tween(this)
            .to( { x: this.targetX, y: this.targetY }, 4000)
            .to( { x: this.x, y: this.y }, 4000)
            .loop(-1)
            .start()
    }
}

class Bat extends Phaser.Sprite {
    constructor(game, x, y, asset) {
        super(game, x, y, asset)
        this.game.physics.arcade.enable(this)
        this.body.allowGravity = false
        this.autoCull = true
        this.body.setSize(49, 17, 9, 18)
        this.body.immovable = true // kinematic

        this.scale.setTo(1.3, 1.3)

        this.animations.add('move', [3, 2, 1, 0], 5, true)
        this.animations.play('move')
    }

    start() {
        // correcao do problema de ancora do TILED
        this.targetY -= this.height

        let tweenA = this.game.add.tween(this)
            .to( { x: this.targetX, y: this.targetY }, 4000)
            .to( { x: this.x, y: this.y }, 4000)
            .loop(-1)
            .start()
    }
}

class Crazy extends Phaser.Sprite {
    constructor(game, x, y, asset) {
        super(game, x, y, asset)
        this.game.physics.arcade.enable(this)
        this.body.allowGravity = false
        this.autoCull = true
        this.body.setSize(32, 32, 15, 0)
        this.body.immovable = true // kinematic

        this.scale.setTo(1.3, 1.3)

        this.animations.add('move', [3, 2, 1, 0], 50, true)
        this.animations.play('move')
    }

    start() {
        // correcao do problema de ancora do TILED
        this.targetY -= this.height

        let tweenA = this.game.add.tween(this)
            .to( { x: this.targetX, y: this.targetY }, 4000)
            .to( { x: this.x, y: this.y }, 4000)
            .loop(-1)
            .start()
    }
}