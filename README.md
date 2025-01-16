# atk
awesome-toolkit


Core package for the Annotation Toolkit Framework

Let the compiler do the heavy lifting
Typesafety
Startup speed
Eliminate runtime errors

Alternative for JPA
Generates 'Repositories' at compile time (instant startup for FAAS)

JPA compliant
Auto DB Schema Maintenance (Fast DB comparison, maintains on changes)
Manages FKeys / Indexes / Fields
Auditing

Entity DAO Mapping

Example

@AtkEntity(columnNamingStrategy = AtkEntity.ColumnNamingStrategy.CAMEL_CASE_UNDERSCORE, addAuditFields = true)
@Table(name = "business")
public class Business {

    @Id
    @GeneratedValue()
    @Column(length = 50)
    private String id;

    private String idN, passportN, passportCountry, vendorCode;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'PENDING_VERIFICATION'")
    @Index(name = "idx_bus_verified_status", columns = {"verifiedStatus"})
    private VerifiedStatus verifiedStatus;

    @ForeignKey(table = User.class, field = "id", name = "businessUserIdFk", onDeleteAction = Cascade)
    @Column(length = 50)
    private String primaryUserId;


}

Will compare entity to DB
If table doesn't exist or differs will generate a DDL to create or update the table (only if entity is newer than schema changes)

	create table business
	(
	created_by varchar(255) null,
	created_date datetime null,
	last_modified_by varchar(255) null,
	last_modified_date datetime null,
	id varchar(50) not null
	primary key,
	id_n varchar(255) null,
	passport_n varchar(255) null,
	passport_country varchar(255) null,
	vendor_code varchar(255) null,
	verified_status varchar(255) default 'PENDING_VERIFICATION' null,
	primary_user_id varchar(50) null,
	constraint business_ibfk_1
	foreign key (primary_user_id) references kwebo_prd.user (id)
	on delete cascade
	);

	create index idx_bus_verified_status
	on kwebo_prd.business (verified_status);


This will also generate a new Class BusinessEntity repository via annotation processing
Lazy loading and entity relationships are also supported
Ability to only do a sub select of columns for performance

	BusinessEntity be = new BusinessEntity().setPasportN("somepass").query().getBySet(c);

Also includes auto mapping to DAO classes and many other features

Other examples

	TripEntity tripEntity = new TripEntity().setId(Long.valueOf(tripId)).query().retrieve(c)
                  .setNightDriveAlarmId(alarmEntity.getId())
                  .persist().update(c);
