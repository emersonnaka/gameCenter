class Config {}
Config.WIDTH = 800
Config.HEIGHT= 400
Config.DEBUG = true
Config.ANTIALIAS = true
Config.ASSETS = 'assets/'

class Game extends Phaser.Game {
    constructor() {
        super(Config.WIDTH, Config.HEIGHT, Phaser.CANVAS, 'game-container', null, false, Config.ANTIALIAS)

        this.state.add('Play', PlayState, false)
        this.state.start('Play')
    }
}

class PlayState extends Phaser.State {
	preload() {
		this.game.load.tilemap('level1', Config.ASSETS + 'phase1.json', null, Phaser.Tilemap.TILED_JSON)
		this.game.load.image('tiles', Config.ASSETS + 'tiles/tiles.png')
		this.game.load.image('objects', Config.ASSETS + 'objects/objects.png')
		this.game.load.image('background', Config.ASSETS + 'background.png')

		this.game.load.spritesheet('character', Config.ASSETS + 'character/character.png', 75, 90)
	}

	create() {
		this.game.physics.startSystem(Phaser.Physics.ARCADE)
        this.game.stage.backgroundColor = '#000000'
		
		let bg = this.game.add.tileSprite(0, 0, Config.WIDTH, Config.HEIGHT, 'background')
        bg.fixedToCamera = true

        this.keys = this.game.input.keyboard.createCursorKeys()
        this.game.physics.arcade.gravity.y = 550

        this.createMap()
        this.createPlayer()

        let keys = this.game.input.keyboard.createCursorKeys()
	}

	createMap() {
		this.map = this.game.add.tilemap('level1')
        this.map.addTilesetImage('tiles')
        this.map.addTilesetImage('objects')

        this.mapLayer = this.map.createLayer('Tile Layer')

        this.map.setCollisionBetween(1, 18, true, 'Tile Layer')
        // this.map.setCollisionBetween(6, 8, true, 'Tile Layer')
        // this.map.setCollisionBetween(10, 13, true, 'Tile Layer')
        // this.map.setCollisionBetween(16, 18, true, 'Tile Layer')
        this.mapLayer.resizeWorld()
	}

	createPlayer() {
		this.player = new Player(this.game, this.keys, 50, 100, 'character')
		this.game.add.existing(this.player)
		this.game.camera.follow(this.player, Phaser.Camera.FOLLOW_LOCKON, 0.1, 0.1)
	}

	update() {
		this.game.physics.arcade.collide(this.player, this.mapLayer)
	}

	render() {
		if(Config.DEBUG) {
			this.game.debug.body(this.player)
		}
	}
}

const GAME = new Game()