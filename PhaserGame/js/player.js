class Player extends Phaser.Sprite {
    constructor (game, cursors, x, y, asset) {
        super(game, x, y, asset)
        this.keys = cursors
        this.game.physics.enable(this, Phaser.Physics.ARCADE)
        this.body.collideWorldBounds = true
        this.body.setSize(65, 85, 8, 0)
        this.anchor.setTo(0.5, 0.5)

        this.animations.add('idle', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], 10, true)
        this.animations.add('walk', [10, 11, 12, 13, 14, 15, 16, 17, 18, 19], 10, true)
        // this.animations.add('jump', [3], 10, true)

        // let jumpButton = this.game.input.keyboard.addKey(
        //     Phaser.Keyboard.SPACEBAR)
        // jumpButton.onDown.add(this.jump, this)
    }

    update() {
        this.body.velocity.x = 0

        if (this.keys.left.isDown)
            this.body.velocity.x = -30
        else if (this.keys.right.isDown)
            this.body.velocity.x = 30

        this.animate()
    }

    animate() {
    	// Andando ou parado
    	if (this.body.velocity.x != 0)
            this.animations.play('walk')
        else
            this.animations.play('idle')

        // Define o lado
        if (this.body.velocity.x > 0)
            this.scale.x = 1
        else if (this.body.velocity.x < 0)
            this.scale.x = -1
    }

}