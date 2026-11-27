package org.example

// Animal information required for care planning
case class CareAnimal(
                       id: Int,
                       species: String,
                       careRequirements: String,
                       resourceRequirements: String
                     )

// Staff information and working hours
case class CareStaff(
                      id: Int,
                      name: String,
                      qualifications: Set[String],
                      available: Boolean,
                      startTime: Int,
                      endTime: Int
                    )

// Available facilities
case class CareFacility(
                         id: Int,
                         name: String,
                         suitableSpecies: Set[String],
                         features: Set[String],
                         available: Boolean
                       )

// Available equipment
case class CareEquipment(
                          id: Int,
                          equipmentType: String,
                          quantity: Int,
                          available: Boolean
                        )

// Care activity and its constraints
case class CareActivity(
                         id: Int,
                         animalId: Int,
                         name: String,
                         duration: Int,
                         earliestStart: Int,
                         latestFinish: Int,
                         requiredSkill: String,
                         requiredFacility: String,
                         requiredEquipment: Set[String],
                         dependencies: Set[Int],
                         minimumGap: Int = 0
                       )

// A successfully scheduled activity
case class ScheduledActivity(
                               activity: CareActivity,
                              staff: CareStaff,
                              facility: CareFacility,
                              equipment: List[CareEquipment],
                              startTime: Int,
                              endTime: Int
                            )

// Activities that could not be scheduled
case class UnscheduledActivity(
                                activity: CareActivity,
                                reason: String
                              )

// Final care plan result
case class CarePlanResult(
                           scheduled: List[ScheduledActivity],
                           unscheduled: List[UnscheduledActivity]
                         )