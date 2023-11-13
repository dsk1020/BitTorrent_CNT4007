# Peer-to-Peer BitTorrent Protocol for File Distribution
_Nikolas Praschma(nikolas.praschma@ufl.edu)_ 
_David Skripnikov(dskripnikov@ufl.edu)_
_Renee Kaynor(m.kaynor@ufl.edu)_

## Team Collaboration
All team members collaborated effectively to establish the initial peer-to-peer socket and client connections.

### Nick
- Implemented the chocking/unchoking mechanism.
- Handled parsing and exportation of downloaded files.

### David
- Developed functionality for parsing, sending, reading, and interpreting various types of messages.

### Renee
- Utilized configuration file information to construct each Peer Process.
- Implemented handling for Bitfield messages.

## Demo of Project
_Include link_

## What we were able to accomplish
Our project successfully implemented the establishment of a Peer-to-Peer network to share a file using TCP. This process involves the sending of handshake choke, unchoke, interested, not interested, have, bitfield, request, and piece messages.

### Handshake Message
The handshake consists of three parts: handshake header, zero bits, and peer ID. The header is used to identify that a message is a handshake message, and the peer ID is used to match the peers up with each other.

An example of Peer 1002 successfully establishing a connection with Peer 1001 using a handshake message in **log_peer_1002.log**:
`01:41:28: Peer 1002 makes a connection to Peer 1002.
01:41:28: Peer 1002 is connected from Peer 1001.`

### Actual messaging
Actual messages include the message length, type, and payload. The type is used to interpret the message so each peer knows how to respond.

### Choke and unchoke
Peers share file information with preferred neighbors and an opmtimistically unchoked neighbor. Each peer uploads its pieces to at most k preferred neighbors and 1 optimistically unchoked neighbor. The value of k is given as a parameter in the Common.cfg file. Each peer uploads its pieces only to preferred neighbors and an optimistically unchoked neighbor. We say these neighbors are unchoked and all other neighbors are choked. The interval in which peers select new neighbors and a new optimistically unchoked neighbor is determined by the Common.cfg file

An example of Peer 1003 being choked and unchoked by different neighbors in **log_peer_1003.log**:
`01:41:43: Peer 1003 is unchoked by 1002
01:41:45: Peer 1003 is choked by 1001`

An example of the preferred neighbors and optimistically unchoked neighbor of Peer 1007 at a given point in time in **log_peer_1007.log**:
`01:42:30: Peer 1007 has the preferred neighbors 1008,1006,1001.
01:42:30: Peer 1007 has the optimistically unchoked neighbor 1001.`

### Interested and Not Interested
Peers send interested messages to other peers for pieces of the file that it needs. If a neighbor does not have any interesting pieces,
then the peer sends a ‘not interested’ message to the neighbor.

An example of interested and not interested messages being received by Peer 1004 in **log_peer_1004.log**:
`01:42:10: Peer 1004 received the 'interested' message from 1005
01:42:10: Peer 1004 received the 'not interested' message from 1001`

### Have and Bitfield
Peers send have messages when they receive a piece. They 


## How to run the project
