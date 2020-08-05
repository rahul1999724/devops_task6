job('task6-job1') {
    description('job1')
	scm {
        	github('rahul1999724/devops_task6')
    }
    steps {
       		shell("cp * -vrf /home/jenkins") 
     	}
  	triggers {
        	scm('* * * * *')
	    	}
	triggers {
        		upstream('Admin Job (Seed)', 'SUCCESS')
    			}
}

job('task6-job2') {
    description('job2')
	scm {
        	github('rahul1999724/devops_task6')
    		}
	triggers {
        		upstream('task6-job1', 'SUCCESS')
    			}
	steps {
       		shell('''
			if  ls /home/jenkins | grep php
			then
			 	if kubectl get deployment --selector "app in (httpd)" | grep httpd-web
    				then
			 		kubectl apply -f Deployment.yml
    			 	else
                 			kubectl create -f Deployment.yml
    			 	fi
    			 	POD=$(kubectl get pod -l app=httpd -o jsonpath="{.items[0].metadata.name}")
    			 	kubectl cp /home/jenkins/index.php ${POD}:/var/www/html
			fi
			''') 
     		}
  
}
job('task6-job3') {
    description('job3')
	triggers {
        		upstream('task6-job2', 'SUCCESS')
    			}
    steps {
       	shell('''
		status=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.43.16:9001)
		if [[ $status == 200 ]]
		then
			exit 0
		else
			exit 1
	    	fi
		''') 
     			}
  publishers {
        extendedEmail {
            recipientList('rahulkumar2514@gmail.com')
            defaultSubject('Job status')
          	attachBuildLog(attachBuildLog = true)
            defaultContent('Status Report')
            contentType('text/html')
            triggers {
                always {
                    subject('build Status')
                    content('Body')
                    sendTo {
                        developers()
                        recipientList()
                    }
                }
            }
        }
    }
}
