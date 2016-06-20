// Command.h

#ifndef _COMMAND_h
#define _COMMAND_h
#include <WString.h>
#include <ArduinoJson.h>
#include "MsgQueClient.h"
#include "FacilityConfiguration.h"

#define CMD_HINT_CONFIRM_RESERVATION_REQ				"confirm_reservation"
#define CMD_HINT_CONFIRM_RESERVATION_RESP				"confirm_reservation_resp"	// defined in controller

#define CMD_HINT_PAYMENT_REQ							"confirm_exit"
#define CMD_HINT_PAYMENT_RESP							"confirm_exit_resp"	// defined in controller

#define CMD_HINT_CAR_PARKED_NOTIFY						"slot"
#define CMD_HINT_MY_STATUS_NOTIFY						"alive"

class Command
{	
protected:
	static int topicId;

	String topic;
	String body;
public:
	Command();

	int getFacilityId();
	int getTopicId();

	void setTopic(String topic);
	virtual String getTopic();

	void setBody(String body);
	void setBody(char * body);
	virtual String getBody();
	
	virtual bool send(MsgQueClient *_client);

	String toString();
};
#endif

