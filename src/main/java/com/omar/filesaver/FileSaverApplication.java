package com.omar.filesaver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@SpringBootApplication
public class FileSaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileSaverApplication.class, args);
	}

}


@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
class Product {
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Id
	public Long id;
	public String name, image;
}

@Repository
interface ProductRepo extends JpaRepository<Product,Long>{

}

@Controller
class ProductController {

	private final Path rootLocation;
	@Autowired
	ProductRepo productRepo;
	@Autowired
	public ProductController() {
		this.rootLocation = Paths.get("upload-dir");
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping
	public String addProduct() {
		return "addProductForm";
	}

	@PostMapping
	public String saveProduct(NewProductForm newProductForm){
		try {
			final String imagePath = "upload-dir/"; //path
			FileOutputStream output = new FileOutputStream(imagePath+newProductForm.image.getOriginalFilename());
			output.write(newProductForm.image.getBytes());
			// save product to db
			productRepo.save(new Product(null, newProductForm.name, newProductForm.image.getOriginalFilename()));
			return "addProductForm";
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to store file.", e);
		}
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		Path fpath = rootLocation.resolve("0e09abec-3feb-48a4-aeef-309481afee6c.JPG");
		try {
			Resource file = new UrlResource(fpath.toUri());
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
@AllArgsConstructor
@NoArgsConstructor
@Data
class NewProductForm {
	public String name;
	public MultipartFile image;
}