#include <stdio.h>
#include <stdlib.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <android/log.h>

#include "search.h"






//----------------------------------------------------------------------
// Exported functions
//----------------------------------------------------------------------
/*
 *  @brief  Decode a sample
 */

int init(int *bswSocket, struct BrowsePacket *Packet, unsigned short cmd, char *localAddr)
{
	//clear();


	struct sockaddr_in local_addr_in;
	int ret, so_broadcast = 1;

	// fill structure
	// Add broadcast header data to buffer	
	Packet->Header.Tag[0] = 'A';
	Packet->Header.Tag[1] = 'v';
	Packet->Header.Tag[2] = 'C';
	Packet->Header.Tag[3] = 'm';
	Packet->Header.Ver[0] = '1';
	Packet->Header.Length = sizeof(struct BrowseHeader);
	Packet->Header.ProductID[0] = (char)0xFF;
	Packet->Header.ProductID[1] = (char)0xFF;
	Packet->Header.ProductID[2] = (char)0xFF;
	Packet->Header.ProductID[3] = (char)0xFF;
	Packet->Header.Command = cmd;
	Packet->Header.Result = 0;
	Packet->Header.DataType = 0;
	Packet->Header.DataLen = 0;

	// set send data Addr In
	if (localAddr == NULL){
		local_addr_in.sin_addr.s_addr = htonl(INADDR_ANY);
	//__android_log_print(ANDROID_LOG_ERROR, "zzz", "null");
	}
	else{
		local_addr_in.sin_addr.s_addr = inet_addr(localAddr);	
		//__android_log_print(ANDROID_LOG_ERROR, "zzz", "else");
	}
	local_addr_in.sin_family = AF_INET;
	local_addr_in.sin_port = htons(0);

	// *bswSocket = socket(AF_INET, SOCK_DGRAM,IPPROTO_IP);
	*bswSocket = socket(AF_INET, SOCK_DGRAM,IPPROTO_UDP);
	setsockopt(*bswSocket, SOL_SOCKET, SO_BROADCAST, (char*)&so_broadcast, sizeof(so_broadcast));
	ret = bind(*bswSocket, (struct sockaddr*)&local_addr_in, sizeof(local_addr_in));

	if(ret == -1)
		return -100;



  return ret;
}
