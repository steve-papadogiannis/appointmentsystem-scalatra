package com.example.app.model

import java.sql.Timestamp

case class Appointment(id: Option[Long], doctorId: Long, patientId: Long, startDateTime: Timestamp, endDateTime: Timestamp,
                       creationDate: Timestamp, description: String) {

}
