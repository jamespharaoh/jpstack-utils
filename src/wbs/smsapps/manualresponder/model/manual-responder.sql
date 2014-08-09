
CREATE TABLE manual_responder_reply_message (

	manual_responder_reply_id int
	NOT NULL
	REFERENCES manual_responder_reply,

	index int
	NOT NULL,

	PRIMARY KEY (
		manual_responder_reply_id,
		index
	),

	message_id int
	NOT NULL
	REFERENCES message

);
