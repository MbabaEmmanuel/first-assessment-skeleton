import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'
// import { Timestamp } from './Timestamp'

export const cli = vorpal()

let username
let server
let lastCommand
let address
let re = /@/


cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [<ipadress>] = "localhost"')
 .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    address = args.address
    server = connect({ host: address, port: 8080 }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
    }
  )

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
callback()
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input,/[^\s]+/g)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      server.on('data', (buffer) => {

        this.log(Message.fromJSON(buffer).toString() + '\n')
          this.log(Message.fromJSON(buffer).toString() + '\n' )

      })


    } else if (command === 'users') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      server.on('data', (buffer) => {

        this.log(Message.fromJSON(buffer).toString())

      })


    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      server.on('data', (buffer) => {

        this.log(Message.fromJSON(buffer).toString())
      })
    } else if (command.charAt(0) === '@') {
      command = '@user'
      server.write(new Message({ username, command, contents, directUser }).toJSON() + '\n')
      server.on('data', (buffer) => {

        this.log(Message.fromJSON(buffer).toString())
      })

    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
