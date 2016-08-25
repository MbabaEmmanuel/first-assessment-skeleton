import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let lastCommand

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [ipaddress]')
 .delimiter(cli.chalk['green']('connected>'))

  .init(function (args, callback)

   {
     lastCommand = 'connect'
      username = args.username

      server = connect({ host: args.ipaddress, port: 8080 }, () =>

      {
        server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      })

      server.on('data', (buffer) => {
        this.log(Message.fromJSON(buffer).toString())
      })

      server.on('end', () => {
        cli.exec('exit')

    })
      callback()
  })
  //Makes a if for all given commands and corresponding functions
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input,/[^\s]+/g)
    let contents = rest.join(' ')

    if (command === 'disconnect')
    {
      server.end(new Message({ username, command }).toJSON() + '\n')
    }
    else if (command === 'echo')
    {

      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      lastCommand = 'echo'
    }

    else if (command === 'users')
    {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    }
    else if (command === 'broadcast')
    {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      lastCommand = 'broadcast'

    }
    else if (command.charAt(0) === '@')
    {
      server.write(new Message({username, command: command, contents }).toJSON() + '\n')
      lastCommand = command
    }
    //if no commands are put in, use the last entered command; sets up the default
    else if (command !== 'disconnect' && command !== 'echo' && command !== 'broadcast'
          && command.charAt(0) !==  '@' && command !== 'users' && lastCommand === 'echo')
    {

      server.write(new Message({ username, command: lastCommand, contents: command + ' ' + contents}).toJSON() + '\n')

    }
    else if (command !== 'disconnect' && command !== 'echo' && command !== 'broadcast'
          && command.charAt(0) !==  '@' && command !== 'users' && lastCommand === 'broadcast')
    {
      server.write(new Message({ username, command: lastCommand, contents: command + ' ' + contents }).toJSON() + '\n')

    }
    else if ( command !== 'disconnect' && command !== 'echo' && command !== 'broadcast'
          && command.charAt(0) !==  '@' && command !== 'users' && lastCommand.charAt(0) === '@')
    {
        server.write(new Message({ username, command: lastCommand, contents: command + ' ' + contents }).toJSON() + '\n')

    }
    else if ( command !== 'disconnect' && command !== 'echo' && command !== 'broadcast'
          && command.charAt(0) !==  '@' && command !== 'users' && lastCommand === 'connect')
    {
        this.log(`Command <${command}> was not recognized`)

    }

    else
    {
        this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
