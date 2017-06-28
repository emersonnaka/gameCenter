class Trophy extends Phaser.Sprite {
    constructor(game) {
        super(game, 0, 0, '')

        this.data = {}
        this.data['ten coins'] = 
        {name: 'ten coins', xp: 10, 
        title: 'Congratulations! You taked 10 coins',
        description: 'The first ten coins on game'}

        this.data['first death'] = 
        {name: 'first death', xp: 10, 
        title: 'Unfortunatly this is your first death',
        description: 'The first death on the game'}

        this.panels = [] // fila de paineis de trofeus
        this.achieved = [] // lista dos nomes do trofeus jah conquistados

        //ServerComm.clearTrophy((r) => console.log( JSON.stringify(r) ) ) 

        // listar os trofeus no servidor e atualizar this.achieved
        ServerComm.listTrophy( 
            (response) => this.updateAchievedTrophies(response) )
    }

    verifyProfile(json){
        let resp = JSON.parse(JSON.stringify(json))
        if (resp['response'] == 'error'){
            console.log(resp['data'])
        }
    }

    updateAchievedTrophies(json) {
        // coloca os nomes dos trofeus na lista de controle: this.achieved
        let list = JSON.parse(JSON.stringify(json))
        for (let t of list) {
            this.achieved.push(t['name'])
            this.addTrophyOnPage(t['name'])
        }
    }

    createPanel(trophyName) {
        let panelY = this.game.height - 74 - this.panels.length * 74
        let panel = this.game.add.sprite(this.game.width - 250, panelY, 'trophy')
        panel.fixedToCamera = true 
        //panel.alpha = 0

        let labelX = 66
        let labelWidth = panel.width - labelX
        let style = {font: '10px Arial', fill: '#ffffff',
            wordWrap: true, wordWrapWidth: labelWidth}
        let label = this.game.add.text(labelX, 5, '', style)
        label.lineSpacing = -7
        panel.addChild(label)

        // define label
        label.text = this.data[trophyName].title + '   +'
        label.text+= this.data[trophyName].xp + '\n\n'
        label.text+= this.data[trophyName].description

        return panel
    }

    show(trophyName) {
        if (this.achieved.includes(trophyName))
            return
            
        ServerComm.addTrophy(this.data[trophyName], 
            (response) => this.onServerResponse(response, trophyName) )
    }

    onServerResponse(response, trophyName) {
        if (response['response'] != 'ok') {
            console.log("ERRO de comunicao com o servidor")
            return
        }
        this.achieved.push(trophyName)

        let panel = this.createPanel(trophyName)
        this.panels.push(panel)
        // agenda a destruicao do panel
        this.game.time.events.add(Phaser.Timer.SECOND * 3,
            this.removePanel, this)
        
        this.addTrophyOnPage(trophyName)
    }

    addTrophyOnPage(trophyName) {

        $('#trophies-list-empty-label').hide()

        let html = Templates.trophiesListItem(this.data[trophyName])
        $('#div-trophy').append(html)
    }

    removePanel() {
        let p = this.panels.shift()
        p.destroy()
    }
}

class Templates {}
Templates.trophiesListItem = Handlebars.compile(
    $('#template-trophies-list-item').html()
)
Templates.profileItem = Handlebars.compile(
    $('#template-profile-item').html()
)

class ServerComm {
    static addTrophy(data, callback) {
        ServerComm.sendRequestTrophy(
            'nelson', 'irineus-adventure', 'add-trophy', data, callback)
    }

    static listTrophy(callback) {
        ServerComm.sendRequestTrophy(
            'nelson', 'irineus-adventure', 'list-trophy', '', callback)
    }

    static clearTrophy(callback) {
        ServerComm.sendRequestTrophy(
            'john_doe', 'clear-trophy', '', callback)
    }

    // metodo generico a ser usado por todas as 
    // requisicoes de trofeus
    static sendRequestTrophy(user, gameName, opName, opData, callback) {
        let data = {
            id: user,
            game: gameName,
            op: opName,
            data: opData
        }
        ServerComm.ajaxPost(data, callback)
    }

    static sendQueryProfile(user, opName, password, callback) {
        let data = {
            id: user,
            op: opName,
            data: {password}
        }
        ServerComm.ajaxPost(data, callback)
    }

    static sendCheckpoint(user, gameName, opName, x, y, phase, callback) {
        let data = {
            id: user,
            game: gameName,
            op: opName,
            data: {x, y, phase}
        }
        ServerComm.ajaxPost(data, callback)
    }

    static ajaxPost(data, callback) {
        let url = 'http://localhost:8081/game/profile'
        $.post(url, JSON.stringify(data))
            .done(function(data, status) {
                console.log(data)
                let jsonObj = JSON.parse(JSON.stringify(data))
                callback(jsonObj)
            })
            .fail(function(jqXHR, status, errorThrown) {
                console.log('ERROR: cannot reach game server')
            })
    }
}