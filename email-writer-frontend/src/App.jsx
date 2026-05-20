  
import { Container, TextField, Box, Typography, Button, CircularProgress } from '@mui/material';
import { FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import './App.css'
import { useState } from 'react';
import axios from 'axios';

function App() {

  const[emailContent , setEmailContent ] = useState(''); 
  const[tone , setTone ] = useState(''); 
  const [generatedReply, setGeneratedReply] = useState('');
  const[loading , setLoading ] = useState(false ); 

  const handleSubmit = async () =>
  {
    setLoading(true);
    try {
      const response= await axios.post("http://localhost:8080/api/email/generate" ,{
        emailContent,
        tone  
      });
      setGeneratedReply(
           typeof response.data === 'string'
           ? response.data
           : JSON.stringify(response.data)
       );
    } catch (error) {
      
    }finally{
      setLoading(false);
    }

  };


  return (
    <>
      <Container maxWidth="md" sx={{py:4}}>
        <Typography variant='h3' component="h1" gutterBottom >
          Email Reply Generator
        </Typography>
        <Box sx={{mx:3}}>
           <TextField
              fullWidth
              multiline
              rows={6}
              variant="outlined"
              label="Original Email Content"
              value={emailContent || ''}
              onChange={(e) => setEmailContent(e.target.value)}
              sx={{ mb:2 }}
           />
             <br></br>
             <br></br>
           <FormControl fullWidth sx={{ mb:2 }} >
           <InputLabel >Tone</InputLabel>
           <Select
           
           value={tone || ''}
           label="Age"
          onChange={(e) => setTone(e.target.value)}
           >
            <MenuItem value="None">None</MenuItem>
            <MenuItem value="Professional">Professional</MenuItem>
            <MenuItem value="Casual">Casual</MenuItem>
            <MenuItem value="Friendly">Friendly</MenuItem>
           </Select>
          </FormControl>

          <Button variant="contained"
          sx={{ mb:2 }}
          onClick={handleSubmit}
          disabled={!emailContent || loading}>
          {loading ? <CircularProgress size={24}/> : "Generate Reply"}
          </Button>
        </Box>
        <Box sx={{mx:3}}>
           <TextField
              fullWidth
              multiline
              rows={6}
              variant="outlined"
              value={generatedReply || ''}
              inputProps={{readonly : true}}
              sx={{ mb:2 }}
           /></Box>
 
         <Button
            variant='outlined'
            onClick={() => navigator.clipboard.writeText(generatedReply)}>
          Copy to clipboard
         </Button>

      </Container>
    </>
  )
}

export default App

//   /v1beta/models/gemini-2.0-flash:generateContent