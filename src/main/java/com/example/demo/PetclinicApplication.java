package com.example.demo;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
/*
interface PersonRepository extends ListCrudRepository<Person, Integer> {
    @Query("select * from person where name = :name")
    Collection<Person>findByName(String name);
}
*/
@SpringBootApplication
public class PetclinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetclinicApplication.class, args);




    }
    record Dog(int id,String name, String owner,String description){}
    @Bean
    Step step(JobRepository repository, PlatformTransactionManager transactionManager, ItemReader<Dog> reader, ItemWriter<Dog> writer) {
        return new StepBuilder("step",repository)
                .<Dog,Dog>chunk(10,transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }
    @Bean
    ItemReader<Dog> reader(@Value("file:C:/Users/Saad/Downloads/dogs.csv") Resource resource) {
        //todo
     return new FlatFileItemReaderBuilder<Dog>()
             .resource(resource)
             .name("dogReader")
             .delimited().names("linenum,id,name,description,dob,owner,gender,image".split(","))
             .linesToSkip(1)
             .fieldSetMapper(new FieldSetMapper<Dog>() {

                 @Override
                 public Dog mapFieldSet(FieldSet fieldSet) throws BindException {
                     return new Dog(fieldSet.readInt("id"),
                             fieldSet.readString("name"),
                             fieldSet.readString("owner"),
                             fieldSet.readString("description"));
                 }
             })
             .build();
    }
    @Bean
    ItemWriter<Dog> writer(DataSource dataSource) {
        //todo
        /*
        return new ItemWriter<Dog>() {

            @Override
            public void write(Chunk<? extends Dog> chunk) throws Exception {
                System.out.println("------------------------------");
                chunk.forEach(System.out::println);
                System.out.println("===============================");
            }
        };
        */
        return new JdbcBatchItemWriterBuilder<Dog>()
                .dataSource(dataSource)
                .sql("INSERT INTO DOG(id,name,description,owner) VALUES (?,?,?,?)")
                .assertUpdates(true)
                .itemPreparedStatementSetter(new ItemPreparedStatementSetter<Dog>() {

                    @Override
                    public void setValues(Dog item, PreparedStatement ps) throws SQLException {
                        ps.setInt(1,item.id());
                        ps.setString(2,item.name());
                        ps.setString(3,item.description());
                        ps.setString(4,item.owner());
                    }
                })
                .build();
    }
    @Bean
    Job job(JobRepository repository, Step step) {
     return new JobBuilder("job",repository)
             .incrementer(new RunIdIncrementer())
             .start(step)
             .build();
    }
    /*
@Bean
ApplicationRunner runner(PersonRepository repository) {
        return args -> System.out.println(repository.findByName("saad"));
}*/
}
/*
record Person(String name, @Id int id, Set<Dog> dogs) {}
record Dog(int id,String name, String person,String description) {}
*/


