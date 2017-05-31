class Player extends Phaser.Sprite {
    constructor (game, cursors, x, y, asset) {
        super(game, x, y, asset)
        this.keys = cursors
        this.game.physics.enable(this, Phaser.Physics.ARCADE)
        this.body.collideWorldBounds = true
        this.body.setSize(38, 45, 5, 11)
        this.anchor.setTo(0.5, 0.5)
        this.lifes = 0
        this.isAttack = false;

        this.animations.add('idle', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], 10, true)
        this.animations.add('run', [10, 11, 12, 13, 14, 15, 16, 17, 18, 19], 20, true)
        this.animations.add('jump', [20], 10, true)
        this.animations.add('attack', [21, 22, 23, 24, 25, 26, 27, 28, 29, 30], 30, true)


        let jumpButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.SPACEBAR)
        jumpButton.onDown.add(this.jump, this)

        let attackButton = this.game.input.keyboard.addKey(Phaser.Keyboard.CONTROL)
        attackButton.onDown.add(this.attack, this)
    }

    update() {
        this.body.velocity.x = 0

        if (this.keys.left.isDown)
            this.body.velocity.x = -150
        else if (this.keys.right.isDown)
            this.body.velocity.x = 150

        this.animate()
    }

    animate() {
    	// Andando ou parado
    	if (this.body.velocity.x != 0)
            this.animations.play('run')
        else
            this.animations.play('idle')

        // No ar
        if (this.body.velocity.y != 0)
            this.animations.play('jump')
        else {
            if(this.isAttack) {
                this.animations.play('attack')
                this.isAttack = false;
            }
        }

        // Define o lado
        if (this.body.velocity.x > 0)
            this.scale.x = 1
        else if (this.body.velocity.x < 0)
            this.scale.x = -1
    }

    jump() {
        if (this.body.onFloor()) {
            this.body.velocity.y = -350
        }
    }

    attack() {
        this.isAttack = true;
    }

    subLife() {
        this.lifes -= 1
    }

    addLife() {
        this.lifes += 1
    }

}